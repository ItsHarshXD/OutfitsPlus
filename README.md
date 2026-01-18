# OutfitsPlus

A lightweight cosmetic armor plugin for Paper servers. Let players wear custom cosmetic outfits without affecting their actual armor.

## Features

- 🎩 **Multiple Cosmetic Categories** - Hats, Masks, Tops, Wings, Pants, and Shoes
- 👁️ **Visibility Control** - Players can toggle their own cosmetics or hide others'
- 🎯 **Empty Slot Only** - Cosmetics only show when armor slot is empty (no gameplay impact)
- 🔒 **Permission-Based** - Unlock cosmetics via permissions or commands
- 🌐 **Multi-Language** - Built-in locale system
- ⚡ **Performant** - Uses Bukkit's native equipment API
- 📦 **No Dependencies** - Pure Paper plugin, no external libraries needed

## Requirements

- Paper 1.21.4+
- Java 21+

## Installation

1. Download the latest release
2. Drop `OutfitsPlus.jar` into your `plugins` folder
3. Restart your server
4. Configure cosmetics in `plugins/OutfitsPlus/outfits/`

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/outfits help` | Show help menu | `outfitsplus.use` |
| `/outfits equip <category> <id>` | Equip a cosmetic | `outfitsplus.command.equip` |
| `/outfits unequip <category\|all>` | Unequip cosmetic(s) | `outfitsplus.command.unequip` |
| `/outfits list [category]` | List available cosmetics | `outfitsplus.command.list` |
| `/outfits toggle <own\|others>` | Toggle visibility | `outfitsplus.command.toggle` |
| `/outfits info <category> <id>` | View cosmetic info | `outfitsplus.command.info` |
| `/outfits locale [locale]` | Change language | `outfitsplus.command.locale` |

### Admin Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/outfits reload` | Reload configuration | `outfitsplus.admin` |
| `/outfits give <player> <category> <id>` | Give cosmetic to player | `outfitsplus.admin` |
| `/outfits take <player> <category> <id>` | Take cosmetic from player | `outfitsplus.admin` |
| `/outfits reset <player>` | Reset player data | `outfitsplus.admin` |

## Creating Cosmetics

Create YAML files in `plugins/OutfitsPlus/outfits/<category>/`:

```yaml
id: cool_hat
display-name: "&6Cool Hat"
lore:
  - "&7A really cool hat!"
  - "&7Show it off to your friends."
material: "LEATHER_HELMET"
custom-model-data: 1001
permission: null
default-unlocked: false
priority: 0
metadata:
  rarity: "rare"
  author: "YourServer"
```

### Categories

| Category | Folder | Material |
|----------|--------|----------|
| Hat | `hats/` | LEATHER_HELMET |
| Mask | `masks/` | LEATHER_HELMET |
| Top | `tops/` | LEATHER_CHESTPLATE |
| Wings | `wings/` | ELYTRA |
| Pants | `pants/` | LEATHER_LEGGINGS |
| Shoes | `shoes/` | LEATHER_BOOTS |

## Permissions

| Permission | Description |
|------------|-------------|
| `outfitsplus.use` | Basic plugin access |
| `outfitsplus.admin` | Admin commands |
| `outfitsplus.bypass.unlock` | Use any cosmetic without unlocking |
| `outfitsplus.cosmetic.<category>.<id>` | Use specific cosmetic |

## Configuration

```yaml
# plugins/OutfitsPlus/config.yml

storage:
  type: yaml  # yaml or mysql

debug: false
default-locale: en

defaults:
  show-own-cosmetics: true
  show-others-cosmetics: true
```

## API

OutfitsPlus provides an API for developers:

```java
OutfitsPlusAPI api = OutfitsPlusAPI.getInstance();

// Check if player has cosmetic equipped
api.hasEquipped(player, CosmeticCategory.HAT);

// Equip a cosmetic
api.equip(player, "cool_hat");

// Get all cosmetics
api.getAllCosmetics();
```

## Support

- Issues: [GitHub Issues](https://github.com/itsharshxd/OutfitsPlus/issues)
- Discord: Coming soon

## Little Note

This project was developed by me with assistance from AI. It is my first project created using AI support, and all code has been thoroughly reviewed and verified.