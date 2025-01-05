package me.libreh.manhunt.mixin.game;

import com.mojang.authlib.GameProfile;
import me.libreh.manhunt.config.Config;
import me.libreh.manhunt.config.PlayerData;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.libreh.manhunt.utils.Constants.*;
import static me.libreh.manhunt.utils.Fields.*;
import static me.libreh.manhunt.utils.Methods.*;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    @Unique
    private final ServerPlayerEntity player = ((ServerPlayerEntity) (Object) this);

    @Unique
    private long lastDelay = System.currentTimeMillis();

    @Unique
    private int rate = 1000;

    @Unique
    private int tickCount;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        var playerUuid = this.getUuid();

        tickCount++;
        if (tickCount == 9) {
            if (isPlaying()) {
                if (isRunner(player)) {
                    var world = player.getServerWorld();
                    if (isOverworld(world)) {
                        OVERWORLD_POSITION.put(playerUuid, this.getBlockPos());
                    } else if (isNether(world)) {
                        NETHER_POSITION.put(playerUuid, this.getBlockPos());
                    } else if (isEnd(world)) {
                        END_POSITION.put(playerUuid, this.getBlockPos());
                    }
                }
            }
        }

        if (tickCount == 19) {
            if (SPAM_PREVENTION.get(playerUuid) != 0) {
                if (SPAM_PREVENTION.get(playerUuid) >= 4) {
                    SPAM_PREVENTION.put(playerUuid, SPAM_PREVENTION.get(playerUuid) - 4);
                } else {
                    SPAM_PREVENTION.put(playerUuid, 0);
                }
            }

            tickCount = 0;
        }

        if (isPlaying()) {
            if (isHunter(player)) {
                if (System.currentTimeMillis() - lastDelay > rate) {
                    if (hasTracker(player)) {
                        rate = updateTracker(player);
                    } else {
                        giveTracker(player);
                    }
                    lastDelay = System.currentTimeMillis();
                }
            }
        }
    }

    @Inject(method = "shouldDamagePlayer", at = @At("HEAD"), cancellable = true)
    private void friendlyFireMixin(PlayerEntity attacker, CallbackInfoReturnable<Boolean> ci) {
        if (isPreGame() || isPaused) {
            ci.setReturnValue(false);
        } else if (isPlaying()) {
            if (this.isTeamPlayer(attacker.getScoreboardTeam())) {
                if (!Config.getConfig().globalPreferences.friendlyFire.equals("always")) {
                    if (Config.getConfig().globalPreferences.friendlyFire.equals(PER_PLAYER)) {
                        if (!PlayerData.get(player).friendlyFire || !PlayerData.get(attacker).friendlyFire) {
                            ci.setReturnValue(false);
                            attacker.sendMessage(Text.translatable("chat.manhunt.friendly_fire.per_player").formatted(Formatting.RED), false);
                        }
                    } else if (Config.getConfig().globalPreferences.friendlyFire.equals("never")) {
                        attacker.sendMessage(Text.translatable("chat.manhunt.friendly_fire").formatted(Formatting.RED), false);
                        ci.setReturnValue(false);
                    }
                }
            } else {
                if (headStartTicks != 0 && isHunter(attacker)) {
                    ci.setReturnValue(false);
                }
            }
        }
    }

    @Inject(method = "isInvulnerableTo", at = @At("RETURN"), cancellable = true)
    private void disableDamage(ServerWorld world, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (headStartTicks != 0 && isHunter(player)) {
            cir.cancel();
        }
    }

    @Inject(method = "dropPlayerItem", at = @At("HEAD"), cancellable = true)
    private void dropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> ci) {
        var customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData != null && customData.copyNbt().getBoolean("Remove")) {
            ci.cancel();
        }
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeath(DamageSource source, CallbackInfo ci) {
        if (isPlaying()) {
            if (isRunner(player)) {
                if (RUNNERS_TEAM.getPlayerList().size() == 1) {
                    shouldEnd = true;
                } else {
                    makeHunter(player);
                }
            }
        }
    }
}