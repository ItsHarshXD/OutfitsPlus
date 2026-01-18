package dev.harsh.plugin.outfitsplus.config;

import dev.harsh.plugin.outfitsplus.OutfitsPlus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class ReloadManager {

    private final OutfitsPlus plugin;
    private final List<Reloadable> reloadables = new ArrayList<>();
    private final List<Consumer<ReloadResult>> listeners = new ArrayList<>();

    public ReloadManager(OutfitsPlus plugin) {
        this.plugin = plugin;
    }

    public void register(Reloadable reloadable) {
        reloadables.add(reloadable);
    }

    public void addListener(Consumer<ReloadResult> listener) {
        listeners.add(listener);
    }

    public ReloadResult reload() {
        ReloadResult result = new ReloadResult();

        for (Reloadable reloadable : reloadables) {
            try {
                reloadable.reload();
                result.addSuccess(reloadable.getName());
                plugin.getLogger().info("Reloaded: " + reloadable.getName());
            } catch (Exception e) {
                result.addFailure(reloadable.getName(), e);
                plugin.getLogger().severe("Failed to reload " + reloadable.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        listeners.forEach(listener -> listener.accept(result));

        return result;
    }

    public interface Reloadable {
        String getName();

        void reload() throws Exception;
    }

    public static class ReloadResult {
        private final List<String> successes = new ArrayList<>();
        private final Map<String, Exception> failures = new HashMap<>();

        public void addSuccess(String name) {
            successes.add(name);
        }

        public void addFailure(String name, Exception e) {
            failures.put(name, e);
        }

        public boolean hasSuccesses() {
            return !successes.isEmpty();
        }

        public boolean hasFailures() {
            return !failures.isEmpty();
        }

        public boolean isFullSuccess() {
            return failures.isEmpty();
        }

        public List<String> getSuccesses() {
            return successes;
        }

        public Map<String, Exception> getFailures() {
            return failures;
        }

        public int getTotalCount() {
            return successes.size() + failures.size();
        }

        public int getSuccessCount() {
            return successes.size();
        }

        public int getFailureCount() {
            return failures.size();
        }
    }
}
