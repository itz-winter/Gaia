# Gaia

Packet-level Minecraft anticheat plugin.

This repository contains the source for "Gaia", a packet-level anticheat plugin for Spigot / Paper servers.

What this repository is
- Java Maven project (JDK 17) that builds a Bukkit/Spigot/Paper plugin named `Gaia`.
- Project POM: `pom.xml` (groupId `com.gaiaac`, artifactId `gaia`, version `1.0.0`).
- Main plugin class: `com.gaiaac.gaia.core.GaiaPlugin` (see `src/main/java/.../core/GaiaPlugin.java`).
- Plugin descriptor: `src/main/resources/plugin.yml`.
- Configuration template: `src/main/resources/config.yml`.
- Checks are implemented under `src/main/java/com/gaiaac/gaia/checks/` (combat, movement, player subpackages).

Quick facts (pulled from project files)
- Artifact: `Gaia-1.0.0.jar` (Maven final name is `Gaia-${project.version}`)
- Java language level: 17 (see `pom.xml` compiler settings)
- Runtime API version (plugin.yml): `1.21`
- Plugin main class: `com.gaiaac.gaia.core.GaiaPlugin`
- Declared hard dependency in `plugin.yml`: `packetevents` (PacketEvents must be present on the server)

Requirements
- JDK 17 to build (maven compiler source/target set to 17 in `pom.xml`).
- Maven to build the project.
- A server running Spigot/Paper compatible with API 1.21 (the project declares `api-version: "1.21"` in `plugin.yml`).
- PacketEvents plugin/library available on the server (the plugin lists `packetevents` in `plugin.yml` `depend`).

Build

From the repository root run:

```powershell
mvn clean package
```

If the build succeeds the plugin JAR will be produced under `target/` with the final name `Gaia-1.0.0.jar`.

Install

1. Ensure PacketEvents (and any other listed dependencies) are installed on your server.
2. Copy `target/Gaia-1.0.0.jar` into your server `plugins/` directory.
3. Start (or restart) the server. The plugin's `onEnable` method is `GaiaPlugin` in `com.gaiaac.gaia.core`.

Configuration

- The project includes a configuration template at `src/main/resources/config.yml`.
- On first run the plugin will create a plugin data folder and write a runtime `config.yml` there (standard Bukkit behaviour). Edit that file to tune checks, thresholds, alerts, Discord webhook, and punishments.
- Many individual checks are configurable under the `checks:` section in `config.yml`.

Commands & Permissions

- Command: `/gaia` (see `plugin.yml`)
  - Usage shown in `plugin.yml`: `/gaia <debug|violations|reload|alerts|info>`
- Permissions (from `plugin.yml`):
  - `gaia.admin` — Full access to Gaia commands (default: op)
  - `gaia.alerts` — Receive anticheat alerts (default: op)
  - `gaia.bypass` — Bypass all checks (default: false)

Where to look in the source
- Core plugin lifecycle and initialization: `src/main/java/com/gaiaac/gaia/core/GaiaPlugin.java`
- Packet handling: `src/main/java/com/gaiaac/gaia/core/PacketManager.java`
- Player runtime state: `src/main/java/com/gaiaac/gaia/core/PlayerData.java`
- Check registration and lookup: `src/main/java/com/gaiaac/gaia/checks/CheckManager.java`
- Individual checks are grouped under `src/main/java/com/gaiaac/gaia/checks/` with the following top-level packages:
  - `combat/` — combat-related checks (aim, reach, killaura, autoclicker, etc.)
  - `movement/` — movement checks (speed, flight, elytra, etc.)
  - `player/` — player and packet-level checks (badpackets, inventory, ground spoof, etc.)

Notes & caveats (from the repository)
- This plugin uses PacketEvents to inspect client packets; PacketEvents is a hard dependency in `plugin.yml` and must be present on the server.
- The codebase contains many packet-level checks and a large, configurable `config.yml` (see `src/main/resources/config.yml`).
- Building requires Maven and a JDK capable of Java 17 compilation.

Developer notes
- The project uses the Maven standard layout. Java sources are under `src/main/java` and resources under `src/main/resources`.
- If you want to run static analysis or tests, add appropriate tool configurations (none are bundled in this repository).

Limitations
- This README does not attempt to document the detection logic of each check. For a developer-oriented exploration, read the check classes under `src/main/java/com/gaiaac/gaia/checks/`.
- There is no issue tracker or contribution guide in the repository; open a PR or contact the repository owner if you have one.

License
- No license file is present in the repository. Treat the code as unlicensed unless the project owner adds a license.

Getting help
- Inspect source files mentioned above for implementation details.
- Check `plugin.yml` and `config.yml` for runtime configuration and commands/perms.

---

README generated from the repository contents. It reflects files and settings discovered in the project (pom.xml, plugin.yml, config.yml, and the Java sources under `src/main/java`).
