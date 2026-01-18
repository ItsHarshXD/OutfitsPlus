package dev.harsh.plugin.outfitsplus.api;

import dev.harsh.plugin.outfitsplus.cosmetic.Cosmetic;

import java.util.Collection;

public interface CosmeticProvider {

    String getName();

    Collection<Cosmetic> getCosmetics();

    void reload();
}
