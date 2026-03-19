# Welcome to Gaia

Gaia is a packet-level anticheat plugin designed for Bukkit, Spigot, and Paper servers. Think of it as your server's watchdog, keeping an eye on client packets to ensure fair play. This repository holds the source code for Gaia, built as a Java Maven project targeting Java 17. Once compiled, it becomes a plugin JAR (named `Gaia-1.0.0.jar` in this version).

## What Gaia Does

- **Server-side Protection**: Gaia inspects and evaluates client packets using PacketEvents (a dependency declared in `plugin.yml`).
- **Comprehensive Checks**: Detection checks are neatly organized under `src/main/java/com/gaiaac/gaia/checks/`, with categories like `combat`, `movement`, and `player`.
- **Core Functionality**: The heart of Gaia lives in `src/main/java/com/gaiaac/gaia/core/`, including key components like `GaiaPlugin`, `PacketManager`, and `PlayerData`.

## Quick Facts

- **Maven Coordinates**: `com.gaiaac:gaia:1.0.0` (defined in `pom.xml`).
- **Java Version**: 17 (compiler settings in `pom.xml`).
- **Main Class**: `com.gaiaac.gaia.core.GaiaPlugin` (declared in `plugin.yml`).
- **Configuration**: A runtime template is available in `src/main/resources/config.yml`, listing checks and their default thresholds.

## What's Inside

- **Initialization**: `GaiaPlugin.java` handles startup and PacketEvents integration.
- **Packet Handling**: `PacketManager.java` manages listeners and packet dispatching.
- **Player Data**: `PlayerData.java` keeps track of runtime state and packet-derived info.
- **Check Management**: `CheckManager.java` registers and organizes detection checks.
- **Detection Logic**: Explore the extensive list of checks under `src/main/java/com/gaiaac/gaia/checks/`.