package dev.harsh.plugin.outfitsplus.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public final class SchedulerUtil {

    private static Plugin plugin;

    private SchedulerUtil() {
    }

    public static void init(Plugin pluginInstance) {
        plugin = pluginInstance;
    }

    public static BukkitTask runSync(Runnable task) {
        return Bukkit.getScheduler().runTask(plugin, task);
    }

    public static BukkitTask runAsync(Runnable task) {
        return Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    public static BukkitTask runLater(Runnable task, long delayTicks) {
        return Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    public static BukkitTask runLaterAsync(Runnable task, long delayTicks) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks);
    }

    public static BukkitTask runTimer(Runnable task, long delayTicks, long periodTicks) {
        return Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
    }

    public static BukkitTask runTimerAsync(Runnable task, long delayTicks, long periodTicks) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
    }

    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();
        runAsync(() -> {
            try {
                future.complete(supplier.get());
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public static CompletableFuture<Void> runAsyncFuture(Runnable runnable) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        runAsync(() -> {
            try {
                runnable.run();
                future.complete(null);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public static void runForPlayer(Player player, Runnable task) {
        if (player.isOnline()) {
            runSync(task);
        }
    }

    public static void runForPlayerLater(Player player, Runnable task, long delayTicks) {
        runLater(() -> {
            if (player.isOnline()) {
                task.run();
            }
        }, delayTicks);
    }
}
