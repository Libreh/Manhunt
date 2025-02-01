package me.libreh.manhunt.utils;

import me.libreh.manhunt.game.GameState;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Fields {
    public static GameState gameState = GameState.PREGAME;
    public static MinecraftServer server;
    public static Scoreboard scoreboard;
    public static Team runnersTeam;
    public static Team huntersTeam;
    public static List<UUID> presetModeList = new LinkedList<>();

    public static ServerWorld overworld;
    public static ServerWorld theNether;
    public static ServerWorld theEnd;
    public static List<CompletableFuture<Chunk>> chunkFutureList = new ArrayList<>();

    public static boolean shouldEnd;
    public static boolean isPaused;
    public static int headStartTicks;
    public static int timeLimitTicks;
    public static int pauseTicks;
    public static boolean canStart;
    public static int tickCount;
    public static boolean shouldCancelSaving;
    public static int startTicks;
}
