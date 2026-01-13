# SoloLevelingReforged

[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.java.net/projects/jdk/17/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-green)](https://www.minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-47.4.13+-blue)](https://files.minecraftforge.net/net/minecraftforge/forge/)
[![License](https://img.shields.io/badge/License-All_Rights_Reserved-red)](#license)
[![Languages](https://img.shields.io/badge/Languages-Java_99.5%25-yellow)](#languages)

A comprehensive Minecraft Forge mod that implements a leveling system inspired by the Solo Leveling manhwa, featuring custom weapons, player capabilities, and progression mechanics.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Development Environment Setup](#development-environment-setup)
- [Project Structure](#project-structure)
- [Building](#building)
- [Testing](#testing)
- [Code Style and Conventions](#code-style-and-conventions)
- [Core Systems](#core-systems)
- [System Architecture & Relationships](#system-architecture--relationships)
- [Configuration](#configuration)
- [Asset Creation](#asset-creation)
- [Contributing](#contributing)
- [Dependencies](#dependencies)
- [Build Configuration](#build-configuration)
- [Troubleshooting](#troubleshooting)
- [License](#license)

## Prerequisites

| Component | Version | Notes |
|-----------|---------|-------|
| Java JDK | 17 | Required for development and building |
| Minecraft Forge | 47.4.13+ | Mod loader framework |
| Minecraft | 1.20.1 | Target game version |
| IDE | IntelliJ IDEA (recommended) or Eclipse | Development environment |

## Development Environment Setup

### 1. Clone and Import

```bash
git clone https://github.com/Axtheris/SoloLevelingReforged.git
cd SoloLevelingReforged
```

### 2. IDE Configuration

#### IntelliJ IDEA (Recommended)
1. Open IntelliJ IDEA
2. Select "Open" and navigate to the project directory
3. Select the `build.gradle` file when prompted
4. Wait for Gradle sync to complete

#### Eclipse
1. Run `./gradlew eclipse` to generate Eclipse project files
2. Import the project as an existing Eclipse project

### 3. Authentication Setup (Optional)

For testing with a Minecraft account:

```bash
# Run the setup script
./setup_auth.ps1  # Windows PowerShell
# or
./setup_auth.bat   # Windows Command Prompt
```

This will configure authentication properties for development testing.

## Project Structure

```
src/
├── main/
│   ├── java/net/xelpha/sololevelingreforged/
│   │   ├── commands/           # Custom chat commands
│   │   ├── core/              # Core systems (capabilities, keybindings)
│   │   ├── events/            # Event handlers
│   │   ├── item/              # Custom weapon implementations
│   │   ├── network/           # Network packet handling
│   │   ├── ui/                # UI overlays and screens
│   │   ├── Config.java        # Mod configuration
│   │   ├── ModCreativeTabs.java
│   │   ├── ModItems.java      # Item registry
│   │   ├── ModSounds.java     # Sound registry
│   │   ├── ModToolTiers.java  # Tool material definitions
│   │   └── Sololevelingreforged.java  # Main mod class
│   └── resources/
│       ├── META-INF/
│       │   └── mods.toml      # Mod metadata
│       └── assets/sololevelingreforged/
│           ├── models/        # Item/block models
│           ├── textures/      # Textures
│           └── sounds/        # Sound files
└── generated/
    └── resources/             # Generated assets
```

## Building

### Development Build

```bash
./gradlew build
```

This creates a development JAR in `build/libs/` suitable for testing.

### Clean Build

```bash
./gradlew clean build
```

### IDE Integration

The project includes pre-configured run configurations:

| Configuration | Purpose |
|---------------|---------|
| `client` | Runs Minecraft client with the mod loaded |
| `server` | Runs dedicated Minecraft server with the mod |
| `data` | Generates data files and resources |
| `gameTestServer` | Executes automated game tests |

## Testing

### Client Testing

```bash
./gradlew runClient
```

### Server Testing

```bash
./gradlew runServer
```

### Game Tests

```bash
./gradlew runGameTestServer
```

### Data Generation

```bash
./gradlew runData
```

Generated files will be placed in `src/generated/resources/`.

## Code Style and Conventions

### Java Standards

| Guideline | Description |
|-----------|-------------|
| Java Features | Use Java 17 features appropriately |
| Naming | Follow standard Minecraft Forge conventions |
| Variables/Methods | Use descriptive names |
| Documentation | Comment complex logic |

### Package Structure

| Package | Purpose |
|---------|---------|
| `commands/` | Chat command implementations |
| `core/` | Core game mechanics and systems |
| `events/` | Event handlers and listeners |
| `item/` | Custom item implementations |
| `network/` | Network communication |
| `ui/` | User interface components |

### Resource Naming

| Convention | Description |
|------------|-------------|
| File Names | Use snake_case |
| Resources | Follow Minecraft conventions |
| Mod Prefix | Prefix custom resources with mod ID |

## Core Systems

### Capability System
The mod implements player capabilities for leveling and stat tracking:

- Located in `core/CapabilityRegistry.java`
- Data persistence handled by `core/CapabilityStorage.java`
- Player data synced via network packets

### Weapon System
Custom weapons with unique mechanics:

- Base class: `item/SoloLevelingWeaponItem.java`
- Individual implementations in `item/` package
- Tool tiers defined in `ModToolTiers.java`

### Event System
Handles game events and mod integration:

- Experience events in `events/ExperienceEvents.java`
- Client-side events in `events/ClientEvents.java`
- Core mod events in `events/SoloLevelingEvents.java`

### Network System
Handles client-server communication:

- Packet registration in `network/ModNetworkRegistry.java`
- Capability synchronization via `network/SyncCapabilityPacket.java`
- Stat allocation via `network/AllocateStatPacket.java`

### UI System
Custom overlays and screens:

- System overlay in `ui/SystemOverlay.java`
- Console screen in `ui/SystemConsoleScreen.java`
- Overlay registration in `ui/OverlayRegistry.java`

## System Architecture & Relationships

This section explains how different components of the mod interact and depend on each other, helping contributors understand the codebase structure.

### Data Flow Architecture

```
Player Actions → Events → Core Systems → Network → UI Updates
                                      ↓
                                Database/Files
```

### Component Relationships

| Component | Depends On | Used By | Purpose |
|-----------|------------|---------|---------|
| **Events** | Core systems | All systems | Triggers game logic when player actions occur |
| **Core Systems** | Events, Network | Events, UI | Contains game mechanics and business logic |
| **Network** | Core systems | Client/Server sync | Synchronizes data between client and server |
| **UI** | Core systems, Network | Player interaction | Displays game state and accepts player input |
| **Items** | Core systems | Events, Player inventory | Implements custom weapons and tools |
| **Commands** | Core systems | Player/Admin commands | Provides chat-based mod control |

### Key Integration Points

#### Player Leveling System
- **Trigger**: Events detect experience gain (`ExperienceEvents.java`)
- **Logic**: Core systems calculate level progression (`core/` package)
- **Persistence**: Capability system stores player data (`CapabilityRegistry.java`, `CapabilityStorage.java`)
- **Sync**: Network packets update client UI (`SyncCapabilityPacket.java`)
- **Display**: UI shows current stats and progress (`SystemOverlay.java`, stat displays)

#### Weapon Mechanics
- **Definition**: Tool tiers in `ModToolTiers.java` define weapon stats
- **Implementation**: Base weapon class in `item/SoloLevelingWeaponItem.java`
- **Usage**: Events trigger weapon effects when used in combat
- **Balance**: Configuration values in `Config.java` adjust weapon power
- **UI**: Weapon stats displayed in inventory screens

#### Stat Allocation System
- **Interface**: UI tabs (`StatusTab.java`) provide allocation controls
- **Logic**: Core systems validate and apply stat changes
- **Network**: `AllocateStatPacket.java` sends changes to server
- **Persistence**: Capability system saves stat allocations
- **Events**: Game events respond to stat changes (damage calculation, etc.)

### Configuration Impact Map

| Config Setting | Affects These Systems |
|----------------|----------------------|
| Debug logging | All systems (logging output) |
| Balance parameters | Core systems (damage, experience rates) |
| Feature toggles | Events, UI (enable/disable features) |

### Development Workflow

When making changes:

1. **UI Changes**: Start with `ui/` package, then update core logic if needed
2. **New Mechanics**: Begin with events and core systems, then add UI
3. **Network Features**: Implement server-side first, then client sync
4. **Balance Tweaks**: Modify `Config.java` values and test in-game
5. **New Items**: Create in `item/` package, register in `ModItems.java`

### Testing Strategy

| Component | Test Method | Files to Check |
|-----------|-------------|----------------|
| Events | Game tests | `src/test/`, game test server |
| Network | Multiplayer testing | Client + server logs |
| UI | Client testing | Visual verification, user interaction |
| Core Logic | Unit tests + game tests | Debug logs, capability data |
| Items | Creative mode testing | Inventory, tooltips, usage |

### Common Contribution Scenarios

#### Adding a New Stat Type
- **Core Logic**: Add stat definition in `core/` classes
- **Network**: Create/update sync packets in `network/`
- **UI**: Add display in `ui/components/SLStatDisplay.java` and tabs
- **Events**: Update experience calculation if needed
- **Storage**: Extend capability system for persistence

#### Creating a New Weapon
- **Item**: Create weapon class in `item/` extending `SoloLevelingWeaponItem`
- **Registry**: Register in `ModItems.java`
- **Tool Tiers**: Add tier definition in `ModToolTiers.java`
- **Events**: Add weapon-specific effects in events
- **UI**: Update inventory displays and tooltips

#### Adding UI Features
- **UI Components**: Create new component in `ui/components/`
- **UI Tabs**: Add to appropriate tab in `ui/tabs/`
- **Core Integration**: Connect to core systems for data
- **Network**: Add sync if UI affects server state
- **Events**: Add user interaction events

#### Modifying Balance
- **Config**: Update values in `Config.java`
- **Core Logic**: Adjust calculations in core classes
- **Testing**: Verify changes in-game and with game tests

#### Adding Network Features
- **Packets**: Create packet classes in `network/`
- **Registry**: Register in `ModNetworkRegistry.java`
- **Handlers**: Implement client/server handlers
- **Integration**: Connect to core systems and UI

## Configuration

Configuration is handled through Forge's config system in `Config.java`. Settings include:

- Debug logging options
- Balance parameters
- Feature toggles

## Asset Creation

### Models

| Asset Type | Location |
|------------|----------|
| Item Models | `src/main/resources/assets/sololevelingreforged/models/item/` |
| Block Models | `src/main/resources/assets/sololevelingreforged/models/block/` |

### Textures

| Asset Type | Location |
|------------|----------|
| Item Textures | `src/main/resources/assets/sololevelingreforged/textures/item/` |
| GUI Textures | `src/main/resources/assets/sololevelingreforged/textures/gui/` |

### Sounds

| Asset Type | Location |
|------------|----------|
| Sound Definitions | `src/main/resources/assets/sololevelingreforged/sounds.json` |
| Sound Files | `src/main/resources/assets/sololevelingreforged/sounds/` |

## Contributing

### Getting Started for Contributors

Before contributing, familiarize yourself with the [System Architecture & Relationships](#system-architecture--relationships) section above. Understanding how different components interact will help you make appropriate changes and avoid breaking existing functionality.

### Pull Request Process
1. Fork the repository
2. Create a feature branch from `main`
3. Review the [System Architecture](#system-architecture--relationships) to understand component relationships
4. Make your changes following the established patterns
5. Test thoroughly (client, server, and game tests)
6. Submit a pull request with a clear description of what was changed and why

### Code Review Requirements
- All new code must compile without warnings
- Include appropriate unit tests where applicable
- Follow existing code style and patterns
- Update documentation for significant changes
- Test on both client and server environments
- Ensure changes align with the [system architecture](#system-architecture--relationships)

### Testing Requirements
- Verify functionality in single-player
- Test multiplayer compatibility
- Run game tests: `./gradlew runGameTestServer`
- Ensure no crashes or performance issues

### Asset Contributions
- Follow existing naming conventions
- Optimize texture sizes appropriately
- Test models in-game before submitting
- Include source files for complex models

## Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| Minecraft Forge | 47.4.13+ | Mod loading framework |
| Minecraft | 1.20.1 | Game platform |
| Java | 17 | Runtime and development |

## Build Configuration

### Gradle Properties

| Property | Value | Description |
|----------|-------|-------------|
| JVM Args | 3GB heap allocation | Memory allocation for builds |
| Daemon | Disabled | Stability optimization |
| Mappings | Official Mojang | Source code mappings |

### Run Configurations

| Configuration | Setting | Purpose |
|----------------|---------|---------|
| Working Directory | `run/` | Runtime files location |
| Logging Level | Debug | Detailed logging output |
| Game Test Namespaces | Enabled | Test framework support |

## Troubleshooting

### Common Issues

#### Build Failures
- Ensure Java 17 is properly installed
- Check Gradle wrapper permissions
- Clear Gradle cache: `./gradlew clean`

#### Runtime Issues
- Check `run/logs/debug.log`
- Verify Forge version compatibility
- Test with minimal mod set

#### IDE Issues
- Refresh Gradle project
- Invalidate IDE caches
- Re-import project

### Debug Logging
Enable additional logging in `Config.java` for troubleshooting.

## Credits and Acknowledgments

### Asset Credits
Currently, we are borrowing assets from:

**Zerotekz** - Solo Leveling Arsenal Resourcepack
- Creator: Zerotekz
- Link: https://modrinth.com/resourcepack/solo-leveling-arsenal

We appreciate the amazing work done by the Minecraft resourcepack community that helps bring our mod to life visually.

## License

All Rights Reserved

## Version Information

| Component | Version | Status |
|-----------|---------|--------|
| Mod Version | 1.0-SNAPSHOT | Development |
| Minecraft | 1.20.1 | Target platform |
| Forge | 47.4.13+ | Minimum required |