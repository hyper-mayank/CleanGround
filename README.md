# ⚡ CleanGround Pro — The Modern Multi-Threaded Lag & Entity Sweeper

[![Folia Supported](https://img.shields.io/badge/Folia-Native%20Multi--Threading-green?style=for-the-badge&logo=minecraft)](https://github.com/hyper-mayank/CleanGround)
[![Paper Compatible](https://img.shields.io/badge/Paper%20%26%20Spigot-1.16.5%20--%201.21.x-blue?style=for-the-badge&logo=apache)](https://github.com/hyper-mayank/CleanGround)
[![Java 17+](https://img.shields.io/badge/Java-17%2B-orange?style=for-the-badge&logo=openjdk)](https://github.com/hyper-mayank/CleanGround)
[![Zero Tick Lag](https://img.shields.io/badge/Performance-Zero%20Lag-brightgreen?style=for-the-badge)](https://github.com/hyper-mayank/CleanGround)

**CleanGround Pro** is an ultra-optimized, enterprise-grade ground item and entity sweeper engineered from scratch for modern Minecraft server networks. While legacy cleaner plugins (such as original ClearLag) cause massive server lag spikes, execute single-threaded entity searches, and crash on modern multi-threaded Folia servers, CleanGround Pro delivers **asynchronous region-aware entity sweeping**, smart item preservation, automated TPS emergency triggers, and dynamic Kyori Adventure announcements.

---

## ⚡ Why CleanGround Pro? (Competitor Benchmark)

| Feature / Metric | ⚡ CleanGround Pro | ❌ Legacy ClearLag | ❌ Generic Ground Sweepers |
| :--- | :---: | :---: | :---: |
| **Folia Multi-Threading** | **Native Region Scheduler** | Crashes on Region Boundaries | Single-Threaded Only |
| **Tick Lag on Sweep** | **0.00ms (Async Chunk Queue)** | 50–200ms Server Freeze | Lag Spikes |
| **Smart Item Preservation** | **Diamonds, Netherite, Enchants, Lore** | Basic Material ID list | Deletes Player Valuables |
| **Dropped Item Grace Period** | **Configurable Min-Age Ticks** | No (Deletes Instant Drops) | No |
| **Hostile Mob Culling** | **Per-Chunk Threshold with Boss Immunity** | Hard Kill-All or Nothing | Clunky / Inflexible |
| **Adventure MiniMessage** | **Full RGB Gradients & Hex** | Legacy Section Codes (§) | Legacy & Color codes |
| **Dynamic Audio Warnings** | **Version-Safe Pitch Accents** | None | Outdated Sound Names |
| **Emergency Low-TPS Trigger** | **Automated Dynamic Recovery** | None / Rigid | None |
| **PlaceholderAPI Expansion** | **First-Party Built-In** | None | Separate Addon |

---

## 🚀 Key Features

### 🛡️ 1. Native Folia & Paper Multi-Threading
- Built with a universal platform scheduler bridge (`SchedulerAdapter`) that detects `io.papermc.paper.threadedregions.RegionizedServer`.
- Distributes chunk entity scanning across Folia region threads, preventing `ConcurrentModificationException` and eliminating main-thread freezes.

### 💎 2. Smart Item Preservation Engine
- **Never lose player valuables again**:
  - Automatically preserves **Netherite & Diamond gear**, Smithing Templates, Elytras, Beacons, Shulker Boxes, and Totems.
  - Automatically preserves **Anvil-renamed items** (`protect-named: true`).
  - Automatically preserves **Custom Lore items** (RPG & Crate items).
  - Automatically preserves **Enchanted items**.
  - **Grace Period Protection**: Items dropped less than 5 seconds ago (`minimum-age-ticks: 100`) are never deleted, protecting accidental drops and player trading.

### 👾 3. Smart Hostile Mob Culler
- Prevents mob farm overcrowding without ruining players' farms:
  - Caps hostile monsters per chunk (default: 25) and culls only the excess.
  - Strictly protects **Villagers**, **Named Mobs** (Name Tag), **Tamed Pets** (Dogs, Cats, Horses), and **Bosses** (Wither, Ender Dragon, Elder Guardian, Warden).

### ⚡ 4. Emergency Low-TPS Trigger
- Monitors server TPS in real time. If TPS dips below a configurable threshold (e.g. 17.5 TPS), CleanGround Pro automatically executes an emergency lightweight ground sweep to restore server performance.

### 📢 5. Dynamic Adventure Countdown Alerts
- Fully customizable countdown warnings (60s, 30s, 10s, 3s, 2s, 1s) with vibrant MiniMessage RGB gradients and subtle notification audio ticks.

### 🧩 6. PlaceholderAPI Expansion
- `%cleanground_time_left%`: Seconds until the next scheduled sweep.
- `%cleanground_time_formatted%`: Formatted countdown (`MM:SS`).
- `%cleanground_last_removed%`: Total items and entities cleared in last sweep.
- `%cleanground_ground_items%`: Current count of ground items in loaded worlds.
- `%cleanground_status%`: Engine status (`Active` / `Disabled`).

---

## ⌨️ Commands & Permissions

| Command | Alias | Description | Permission |
| :--- | :--- | :--- | :--- |
| `/cleanground sweep` | `/cg sweep`, `/clearlag`, `/cl` | Force an immediate ground item & entity cleanup | `cleanground.admin` |
| `/cleanground count` | `/cg count`, `/cg stats` | View live count of dropped items, entities, and loaded chunks | `cleanground.admin` |
| `/cleanground cullmobs` | `/cg cullmobs` | Cull excess hostile monsters across all chunks | `cleanground.admin` |
| `/cleanground reload` | `/cg reload` | Hot-reload all configuration settings and timers | `cleanground.admin` |
| `/cleanground help` | `/cg help` | Display interactive command guide | `cleanground.admin` |

---

## ⚙️ Configuration (`config.yml`)

<details>
<summary><b>Click to expand full default config.yml</b></summary>

```yaml
# =======================================================
#               CleanGround Pro Configuration
#       Next-Gen Multi-Threaded Ground & Entity Sweeper
# =======================================================

sweep:
  enabled: true
  interval-seconds: 300
  warning-times:
    - 60
    - 30
    - 10
    - 3
    - 2
    - 1
  remove-empty-vehicles: true
  remove-projectiles: true

item-filter:
  protect-named: true
  protect-lore: true
  protect-enchanted: true
  minimum-age-ticks: 100
  whitelist:
    - "DIAMOND"
    - "DIAMOND_BLOCK"
    - "DIAMOND_SWORD"
    - "DIAMOND_PICKAXE"
    - "DIAMOND_AXE"
    - "DIAMOND_SHOVEL"
    - "DIAMOND_HOE"
    - "DIAMOND_HELMET"
    - "DIAMOND_CHESTPLATE"
    - "DIAMOND_LEGGINGS"
    - "DIAMOND_BOOTS"
    - "NETHERITE_INGOT"
    - "NETHERITE_BLOCK"
    - "NETHERITE_SWORD"
    - "NETHERITE_PICKAXE"
    - "NETHERITE_AXE"
    - "NETHERITE_SHOVEL"
    - "NETHERITE_HOE"
    - "NETHERITE_HELMET"
    - "NETHERITE_CHESTPLATE"
    - "NETHERITE_LEGGINGS"
    - "NETHERITE_BOOTS"
    - "NETHERITE_UPGRADE_SMITHING_TEMPLATE"
    - "ELYTRA"
    - "TOTEM_OF_UNDYING"
    - "BEACON"
    - "NETHER_STAR"
    - "DRAGON_EGG"
    - "ENCHANTED_GOLDEN_APPLE"
    - "SHULKER_BOX"
    - "WHITE_SHULKER_BOX"

mob-culling:
  enabled: true
  max-hostiles-per-chunk: 25
  cull-during-sweep: true
  protect-named: true
  protect-tamed: true
  protect-villagers: true
  protect-bosses: true

tps-protection:
  enabled: true
  tps-threshold: 17.5
  cooldown-seconds: 180

sounds:
  warning: "UI_BUTTON_CLICK"
  sweep: "ENTITY_EXPERIENCE_ORB_PICKUP"
  emergency: "BLOCK_NOTE_BLOCK_BASS"

messages:
  prefix: "<gradient:#00d2ff:#3a7bd5>[CleanGround]</gradient> "
  warning: "<yellow>⚠ Ground items will be swept in <gold><bold>%time%s</bold></gold>!</yellow>"
  warning-final: "<red>⚡ Ground items will be removed in <white><bold>%time%s</bold></white>!</red>"
  sweep-complete: "<green>✔ Cleared <white><bold>%items%</bold></white> ground items and <white><bold>%mobs%</bold></white> excess mobs.</green> <gray>(%time%ms)</gray>"
  emergency-triggered: "<red>⚠ Low TPS detected (<white>%tps%</white>)! Emergency cleanup executed.</red>"
  count: "<gray>World stats: <white><bold>%items%</bold></white> items, <white><bold>%entities%</bold></white> entities in <white><bold>%chunks%</bold></white> loaded chunks.</gray>"
  reloaded: "<green>CleanGround Pro configuration reloaded successfully.</green>"
  no-permission: "<red>You do not have permission to execute this command.</red>"
```
</details>

---

## 📦 Compatibility & Platform Support
- **Server Loaders**: Bukkit, Spigot, Paper, Purpur, Pufferfish, and Folia (Multi-Threading).
- **Minecraft Releases**: 1.16.5, 1.17.x, 1.18.x, 1.19.x, 1.20.x, 1.21.x+
- **Java**: Java 17, Java 21, and Java 22+.
