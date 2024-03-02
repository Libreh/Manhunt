package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import eu.pb4.playerdata.api.PlayerDataApi;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import static manhunt.config.ManhuntConfig.*;
import static manhunt.game.ManhuntGame.*;
import static manhunt.game.ManhuntState.PLAYING;
import static manhunt.game.ManhuntState.PREGAME;

public class VoteCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("vote")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> castAVote(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                )
        );
    }

    private static int castAVote(ServerCommandSource source, ServerPlayerEntity player) {
        ServerPlayerEntity sourcePlayer = source.getPlayer();

        if (gameState == PREGAME) {
            if (Boolean.parseBoolean(RUNNER_VOTING.get())) {
                int sourcePlayerVotesLeft = Integer.parseInt(String.valueOf(PlayerDataApi.getGlobalDataFor(sourcePlayer, votesLeft)));

                if (sourcePlayerVotesLeft != 0) {
                    PlayerDataApi.setGlobalDataFor(player, runnerVotes, NbtInt.of(Integer.parseInt(String.valueOf(PlayerDataApi.getGlobalDataFor(player, runnerVotes))) + 1));

                    sourcePlayer.sendMessage(Text.translatable("manhunt.chat.votedfor", Text.literal(player.getName().getString()).formatted(Formatting.GREEN)));

                    NbtList nbtList = (NbtList) PlayerDataApi.getGlobalDataFor(sourcePlayer, votesList);

                    boolean hasntBeenVoted = true;

                    for (Object object : nbtList.toArray()) {
                        if (object == player.getName().getString()) {
                            hasntBeenVoted = false;
                        }
                    }

                    if (hasntBeenVoted) {
                        NbtList newList = (NbtList) PlayerDataApi.getGlobalDataFor(sourcePlayer, votesList);
                        newList.add(NbtString.of(player.getName().getString()));
                        PlayerDataApi.setGlobalDataFor(sourcePlayer, votesList, newList);

                        PlayerDataApi.setGlobalDataFor(sourcePlayer, votesLeft, NbtInt.of(Integer.parseInt(String.valueOf(PlayerDataApi.getGlobalDataFor(sourcePlayer, votesLeft))) - 1));

                        boolean anyVotesLeft = false;

                        for (ServerPlayerEntity serverPlayer : source.getServer().getPlayerManager().getPlayerList()) {
                            if (!(PlayerDataApi.getGlobalDataFor(serverPlayer, votesLeft) == NbtInt.of(0))) {
                                anyVotesLeft = true;
                            }
                        }

                        if (!anyVotesLeft) {
                            for (ServerPlayerEntity serverPlayer : source.getServer().getPlayerManager().getPlayerList()) {
                                if (!(PlayerDataApi.getGlobalDataFor(serverPlayer, runnerVotes) == NbtInt.of(0))) {
                                    votedRunners.put(serverPlayer, Integer.parseInt(String.valueOf(PlayerDataApi.getGlobalDataFor(serverPlayer, runnerVotes))));
                                }
                            }

                            Arrays.sort(votedRunners.entrySet().toArray(), (Comparator) (first, second) -> ((Map.Entry<ServerPlayerEntity, Integer>) second).getValue()
                                    .compareTo(((Map.Entry<ServerPlayerEntity, Integer>) first).getValue()));

                            int votedAmount = Integer.parseInt(VOTE_PLACES.get());

                            for (Object object : votedRunners.entrySet().toArray()) {
                                if (votedAmount == 0) {
                                    break;
                                }
                                topVoted.put(((Map.Entry<ServerPlayerEntity, Integer>) object).getKey(), ((Map.Entry<ServerPlayerEntity, Integer>) object).getValue());
                                votedAmount--;
                            }

                            for (ServerPlayerEntity serverPlayer : source.getServer().getPlayerManager().getPlayerList()) {
                                serverPlayer.getScoreboard().clearTeam(serverPlayer.getName().getString());
                                serverPlayer.getScoreboard().addScoreHolderToTeam(serverPlayer.getName().getString(), serverPlayer.getScoreboard().getTeam("players"));
                                serverPlayer.getScoreboard().addScoreHolderToTeam(serverPlayer.getName().getString(), serverPlayer.getScoreboard().getTeam("hunters"));
                            }

                            int votesPlace = 1;

                            boolean pickedRunner = false;

                            for (Object e : topVoted.entrySet().toArray()) {
                                source.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.votedrunner", Text.literal(String.valueOf(votesPlace)), Text.literal(((Map.Entry<ServerPlayerEntity, Integer>) e).getKey().getName().getString()).formatted(Formatting.GREEN), Text.literal(String.valueOf(((Map.Entry<ServerPlayerEntity, Integer>) e).getValue()))), false);
                                if (!pickedRunner) {
                                    source.getServer().getScoreboard().addScoreHolderToTeam(((Map.Entry<ServerPlayerEntity, Integer>) e).getKey().getName().getString(), source.getServer().getScoreboard().getTeam("runners"));
                                    PlayerDataApi.setGlobalDataFor(((Map.Entry<ServerPlayerEntity, Integer>) e).getKey(), runsLeft, NbtInt.of(Integer.parseInt(TOP_VOTED_RUNS.get()) + 1));
                                    pickedRunner = true;
                                }
                                votesPlace++;
                            }

                            AUTO_START.set(true);

                            startGame(source.getServer());
                        }
                    } else {
                        source.sendFeedback(() -> Text.translatable("manhunt.chat.cantvote"), false);
                    }
                } else {
                    source.sendFeedback(() -> Text.translatable("manhunt.chat.novotes"), false);
                }
            } else {
                source.sendFeedback(() -> Text.translatable("manhunt.chat.runnervoting"), false);
            }
        } else if (gameState == PLAYING) {
            source.sendFeedback(() -> Text.translatable("manhunt.chat.playing"), false);
        } else {
            source.sendFeedback(() -> Text.translatable("manhunt.chat.postgame"), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
