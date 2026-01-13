# SoloLevelingReforged

A Minecraft Forge mod that implements a comprehensive leveling system inspired by the Solo Leveling manhwa, featuring custom weapons, player capabilities, and progression mechanics.

## Prerequisites

- Java 17 JDK
- Minecraft Forge 47.4.13+
- Minecraft 1.20.1
- IntelliJ IDEA (recommended) or Eclipse IDE

## Development Environment Setup

### 1. Clone and Import

```bash
git clone <repository-url>
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

- `client`: Runs Minecraft client with the mod
- `server`: Runs dedicated server with the mod
- `data`: Generates data files
- `gameTestServer`: Runs game tests

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
- Use Java 17 features appropriately
- Follow standard Minecraft Forge naming conventions
- Use descriptive variable and method names
- Document complex logic with comments

### Package Structure
- `commands/`: Chat command implementations
- `core/`: Core game mechanics and systems
- `events/`: Event handlers and listeners
- `item/`: Custom item implementations
- `network/`: Network communication
- `ui/`: User interface components

### Resource Naming
- Use snake_case for file names
- Follow Minecraft resource location conventions
- Prefix custom resources with mod ID

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

## Configuration

Configuration is handled through Forge's config system in `Config.java`. Settings include:

- Debug logging options
- Balance parameters
- Feature toggles

## Asset Creation

### Models
- Item models: `src/main/resources/assets/sololevelingreforged/models/item/`
- Block models: `src/main/resources/assets/sololevelingreforged/models/block/`

### Textures
- Item textures: `src/main/resources/assets/sololevelingreforged/textures/item/`
- GUI textures: `src/main/resources/assets/sololevelingreforged/textures/gui/`

### Sounds
- Sound definitions: `src/main/resources/assets/sololevelingreforged/sounds.json`
- Sound files: `src/main/resources/assets/sololevelingreforged/sounds/`

## Contributing

### Pull Request Process
1. Fork the repository
2. Create a feature branch from `main`
3. Make your changes following the established patterns
4. Test thoroughly (client, server, and game tests)
5. Submit a pull request with a clear description

### Code Review Requirements
- All new code must compile without warnings
- Include appropriate unit tests where applicable
- Follow existing code style and patterns
- Update documentation for significant changes
- Test on both client and server environments

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

- Minecraft Forge 47.4.13+
- Minecraft 1.20.1
- Java 17

## Build Configuration

### Gradle Properties
- JVM Args: 3GB heap allocation
- Daemon: Disabled for stability
- Mappings: Official Mojang mappings

### Run Configurations
- Working directory: `run/`
- Logging level: Debug
- Game test namespaces: Enabled

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

## License

All Rights Reserved

## Version Information

- Mod Version: 1.0-SNAPSHOT
- Minecraft Version: 1.20.1
- Forge Version: 47.4.13+