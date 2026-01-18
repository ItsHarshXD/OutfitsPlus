package dev.harsh.plugin.outfitsplus.integration;

public interface Integration {

    String getName();

    boolean isAvailable();

    void enable();

    void disable();

    void reload();
}
