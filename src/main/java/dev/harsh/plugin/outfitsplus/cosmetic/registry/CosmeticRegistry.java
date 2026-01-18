package dev.harsh.plugin.outfitsplus.cosmetic.registry;

import dev.harsh.plugin.outfitsplus.cosmetic.Cosmetic;
import dev.harsh.plugin.outfitsplus.cosmetic.CosmeticCategory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class CosmeticRegistry {

    private final Map<String, Cosmetic> cosmetics = new ConcurrentHashMap<>();
    private final Map<CosmeticCategory, List<Cosmetic>> byCategory = new ConcurrentHashMap<>();

    public void register(Cosmetic cosmetic) {
        cosmetics.put(cosmetic.id(), cosmetic);
        byCategory.computeIfAbsent(cosmetic.category(), k -> new ArrayList<>()).add(cosmetic);
    }

    public void unregister(String id) {
        Cosmetic removed = cosmetics.remove(id);
        if (removed != null) {
            List<Cosmetic> categoryList = byCategory.get(removed.category());
            if (categoryList != null) {
                categoryList.remove(removed);
            }
        }
    }

    public void clear() {
        cosmetics.clear();
        byCategory.clear();
    }

    public Optional<Cosmetic> get(String id) {
        return Optional.ofNullable(cosmetics.get(id));
    }

    public boolean exists(String id) {
        return cosmetics.containsKey(id);
    }

    public Collection<Cosmetic> getAll() {
        return Collections.unmodifiableCollection(cosmetics.values());
    }

    public List<Cosmetic> getByCategory(CosmeticCategory category) {
        return Collections.unmodifiableList(byCategory.getOrDefault(category, Collections.emptyList()));
    }

    public List<Cosmetic> getDefaultUnlocked() {
        return cosmetics.values().stream()
                .filter(Cosmetic::defaultUnlocked)
                .collect(Collectors.toList());
    }

    public List<Cosmetic> getByPriorityDesc(CosmeticCategory category) {
        return getByCategory(category).stream()
                .sorted(Comparator.comparingInt(Cosmetic::priority).reversed())
                .collect(Collectors.toList());
    }

    public int size() {
        return cosmetics.size();
    }

    public int size(CosmeticCategory category) {
        return byCategory.getOrDefault(category, Collections.emptyList()).size();
    }

    public Set<String> getAllIds() {
        return Collections.unmodifiableSet(cosmetics.keySet());
    }

    public Set<CosmeticCategory> getPopulatedCategories() {
        return byCategory.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}
