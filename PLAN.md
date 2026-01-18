# OutfitsPlus Implementation Plan

## Overview
Client-side cosmetics plugin using PacketEvents packet interception to render visual-only cosmetics without modifying server inventory.

## Package Structure
```
dev.harsh.plugin.outfitsplus/
├── OutfitsPlus.java                     # Main plugin class
├── api/                                 # Public API
│   ├── OutfitsPlusAPI.java
│   ├── CosmeticProvider.java
│   ├── event/                           # Custom events
│   └── model/                           # API models
├── cosmetic/                            # Cosmetic domain
│   ├── Cosmetic.java                    # Immutable cosmetic record
│   ├── CosmeticCategory.java            # Enum (HAT, MASK, WINGS, TOP, PANTS, SHOES)
│   ├── MaskType.java                    # Enum (FULL, HALF, FACE_COVER, VISOR)
│   └── registry/
│       ├── CosmeticRegistry.java
│       └── CosmeticLoader.java
├── player/                              # Player state
│   ├── PlayerData.java
│   ├── PlayerDataCache.java
│   └── PlayerVisibilitySettings.java
├── render/                              # Rendering system
│   ├── CosmeticRenderer.java            # Central coordinator
│   ├── RenderContext.java
│   └── slot/                            # Per-slot renderers
│       ├── SlotRenderer.java
│       ├── HeadSlotRenderer.java
│       ├── ChestSlotRenderer.java
│       ├── LegsSlotRenderer.java
│       └── FeetSlotRenderer.java
├── packet/                              # Packet handling
│   ├── listener/
│   │   └── EquipmentPacketListener.java
│   └── sender/
│       └── CosmeticPacketSender.java
├── storage/                             # Storage abstraction
│   ├── StorageProvider.java             # Interface
│   ├── StorageFactory.java
│   └── yaml/
│       └── YamlStorageProvider.java
├── config/                              # Configuration
│   ├── ConfigManager.java
│   ├── PluginConfig.java
│   └── ReloadManager.java
├── locale/                              # Localization
│   ├── LocaleManager.java
│   └── MessageKey.java
├── command/                             # Commands
│   ├── CommandManager.java
│   └── subcommand/
│       ├── SubCommand.java
│       ├── EquipCommand.java
│       ├── UnequipCommand.java
│       ├── ListCommand.java
│       ├── ToggleCommand.java
│       ├── LocaleCommand.java
│       ├── InfoCommand.java
│       └── admin/
│           ├── ReloadCommand.java
│           ├── GiveCommand.java
│           ├── TakeCommand.java
│           └── ResetCommand.java
├── listener/                            # Bukkit listeners
│   ├── PlayerJoinListener.java
│   ├── PlayerQuitListener.java
│   ├── PlayerRespawnListener.java
│   ├── PlayerWorldChangeListener.java
│   └── InventoryListener.java
├── integration/                         # Future integrations
│   ├── IntegrationManager.java
│   ├── itemsadder/
│   └── nexo/
└── util/                                # Utilities
    ├── ItemBuilder.java
    └── SchedulerUtil.java
```

## File Structure (plugins/OutfitsPlus/)
```
plugins/OutfitsPlus/
├── config.yml
├── locales/
│   └── en.yml
├── outfits/
│   ├── hats/
│   ├── masks/
│   ├── wings/
│   ├── tops/
│   ├── pants/
│   └── shoes/
└── playerdata/
    └── {uuid}.yml
```

---

## Implementation Phases

### Phase 1: Core Infrastructure
**Files to create:**
- `config/ConfigManager.java` - Config loading/saving
- `config/PluginConfig.java` - Parsed config values record
- `config/ReloadManager.java` - Hot-reload orchestration
- `locale/LocaleManager.java` - Multi-locale support
- `locale/MessageKey.java` - Message key enum
- `util/ItemBuilder.java` - Fluent ItemStack builder
- `util/SchedulerUtil.java` - Task scheduling helper

**Resources:**
- `resources/config.yml` - Main config
- `resources/locales/en.yml` - Default locale

### Phase 2: Cosmetic System
**Files to create:**
- `cosmetic/CosmeticCategory.java` - Category enum mapping to equipment slots
- `cosmetic/MaskType.java` - Mask sub-type enum
- `cosmetic/Cosmetic.java` - Immutable cosmetic definition record
- `cosmetic/registry/CosmeticRegistry.java` - Central cosmetic storage
- `cosmetic/registry/CosmeticLoader.java` - YAML cosmetic loader

**Resources:**
- Example cosmetic files in `resources/outfits/<category>/`

### Phase 3: Storage & Player Data
**Files to create:**
- `storage/StorageProvider.java` - Storage interface
- `storage/StorageFactory.java` - Provider factory
- `storage/yaml/YamlStorageProvider.java` - YAML implementation
- `player/PlayerData.java` - Mutable player state
- `player/PlayerDataCache.java` - In-memory cache with dirty tracking
- `player/PlayerVisibilitySettings.java` - Visibility toggles

### Phase 4: Packet Rendering
**Files to create:**
- `render/RenderContext.java` - Render context record
- `render/slot/SlotRenderer.java` - Slot renderer interface
- `render/slot/HeadSlotRenderer.java` - HAT/MASK rendering
- `render/slot/ChestSlotRenderer.java` - WINGS/TOP + Elytra compat
- `render/slot/LegsSlotRenderer.java` - PANTS rendering
- `render/slot/FeetSlotRenderer.java` - SHOES rendering
- `render/CosmeticRenderer.java` - Central rendering coordinator
- `packet/listener/EquipmentPacketListener.java` - Intercept ENTITY_EQUIPMENT
- `packet/sender/CosmeticPacketSender.java` - Send cosmetic updates

**File to modify:**
- `OutfitsPlus.java` - Register packet listener

### Phase 5: Bukkit Listeners
**Files to create:**
- `listener/PlayerJoinListener.java` - Load data, resync
- `listener/PlayerQuitListener.java` - Save data, cleanup
- `listener/PlayerRespawnListener.java` - Resync on respawn
- `listener/PlayerWorldChangeListener.java` - Resync on world change
- `listener/InventoryListener.java` - Detect armor changes

### Phase 6: Commands
**Files to create:**
- `command/subcommand/SubCommand.java` - Subcommand interface
- `command/CommandManager.java` - Root command handler
- `command/subcommand/EquipCommand.java`
- `command/subcommand/UnequipCommand.java`
- `command/subcommand/ListCommand.java`
- `command/subcommand/ToggleCommand.java`
- `command/subcommand/LocaleCommand.java`
- `command/subcommand/InfoCommand.java`
- `command/subcommand/admin/ReloadCommand.java`
- `command/subcommand/admin/GiveCommand.java`
- `command/subcommand/admin/TakeCommand.java`
- `command/subcommand/admin/ResetCommand.java`

**File to modify:**
- `resources/plugin.yml` - Add command/permission definitions

### Phase 7: Public API & Events
**Files to create:**
- `api/OutfitsPlusAPI.java` - Singleton API access
- `api/CosmeticProvider.java` - Custom provider interface
- `api/model/CosmeticData.java` - Read-only cosmetic snapshot
- `api/model/PlayerCosmeticState.java` - Read-only player state
- `api/event/CosmeticEquipEvent.java`
- `api/event/CosmeticUnequipEvent.java`
- `api/event/CosmeticRenderEvent.java`

### Phase 8: Integration Stubs
**Files to create:**
- `integration/Integration.java` - Integration interface
- `integration/IntegrationManager.java` - Integration coordinator
- `integration/itemsadder/ItemsAdderIntegration.java` - Stub
- `integration/nexo/NexoIntegration.java` - Stub

---

## Key Configuration Formats

### config.yml
```yaml
storage:
  type: yaml
  mysql:  # Reserved for future
    host: localhost
    port: 3306
    database: outfitsplus
default-locale: en
auto-save-interval: 5
generate-defaults: true
debug: false
defaults:
  show-own-cosmetics: true
  show-others-cosmetics: true
integrations:
  itemsadder:
    enabled: false
  nexo:
    enabled: false
```

### Cosmetic Definition (outfits/hats/royal_crown.yml)
```yaml
id: royal_crown
display-name: "cosmetic.hats.royal_crown.name"
description: "cosmetic.hats.royal_crown.description"
material: LEATHER_HELMET
custom-model-data: 10001
permission: null  # Defaults to outfitsplus.cosmetic.hats.royal_crown
default-unlocked: false
priority: 10
metadata:
  rarity: legendary
```

### Player Data (playerdata/{uuid}.yml)
```yaml
equipped:
  hat: royal_crown
  wings: angel_wings
unlocked:
  - royal_crown
  - angel_wings
visibility:
  own: true
  others: true
locale: en
```

---

## Command Structure
```
/outfits help                           - Show help
/outfits equip <category> <id>          - Equip cosmetic
/outfits unequip <category|all>         - Unequip cosmetic(s)
/outfits list [category]                - List cosmetics
/outfits toggle <own|others>            - Toggle visibility
/outfits locale [locale]                - View/change locale
/outfits info [category] [id]           - View cosmetic info
/outfits reload                         - Reload configs (admin)
/outfits give <player> <cat> <id>       - Give cosmetic (admin)
/outfits take <player> <cat> <id>       - Take cosmetic (admin)
/outfits reset <player>                 - Reset player (admin)
```

---

## Packet Flow
1. Server prepares `ENTITY_EQUIPMENT` packet
2. `EquipmentPacketListener` intercepts via `PacketSendEvent`
3. `CosmeticRenderer` checks viewer/target visibility settings
4. `SlotRenderer` replaces equipment items with cosmetic items (CustomModelData)
5. Modified packet sent to client
6. Client resource pack renders custom models

---

## Critical Files to Modify
- `OutfitsPlus.java` - Full initialization of all systems
- `plugin.yml` - Commands and permissions
- `build.gradle` - No changes needed (PacketEvents already configured)

---

## Verification Plan
1. Build plugin: `./gradlew build`
2. Deploy to Paper 1.21+ test server
3. Test cosmetic equip/unequip commands
4. Verify packet interception shows cosmetics to other players
5. Test visibility toggles (own/others)
6. Test resync on join/respawn/world change
7. Test hot-reload functionality
8. Verify Elytra compatibility (wings hidden when wearing Elytra)
