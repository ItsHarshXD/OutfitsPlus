package dev.harsh.plugin.outfitsplus.player;

import java.util.UUID;

public final class PlayerVisibilitySettings {

    private boolean showOwnCosmetics;
    private boolean showOthersCosmetics;

    public PlayerVisibilitySettings() {
        this.showOwnCosmetics = true;
        this.showOthersCosmetics = true;
    }

    public PlayerVisibilitySettings(boolean showOwnCosmetics, boolean showOthersCosmetics) {
        this.showOwnCosmetics = showOwnCosmetics;
        this.showOthersCosmetics = showOthersCosmetics;
    }

    public boolean canSee(UUID viewer, UUID target) {
        if (viewer.equals(target)) {
            return showOwnCosmetics;
        }
        return showOthersCosmetics;
    }

    public boolean isShowOwnCosmetics() {
        return showOwnCosmetics;
    }

    public void setShowOwnCosmetics(boolean showOwnCosmetics) {
        this.showOwnCosmetics = showOwnCosmetics;
    }

    public boolean isShowOthersCosmetics() {
        return showOthersCosmetics;
    }

    public void setShowOthersCosmetics(boolean showOthersCosmetics) {
        this.showOthersCosmetics = showOthersCosmetics;
    }

    public void toggleOwn() {
        this.showOwnCosmetics = !this.showOwnCosmetics;
    }

    public void toggleOthers() {
        this.showOthersCosmetics = !this.showOthersCosmetics;
    }

    public PlayerVisibilitySettings copy() {
        return new PlayerVisibilitySettings(showOwnCosmetics, showOthersCosmetics);
    }
}
