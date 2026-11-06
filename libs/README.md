# Local LOTMCraft dependency

Download the complete **LOTMCraft 1.13.3** mod for Minecraft 1.21.1 / NeoForge
and place it here as `lotmcraft-1.13.3.jar` before importing or building the project.
The dependency is not distributed in this repository or bundled in the addon.

- [LOTMCraft downloads](https://www.curseforge.com/minecraft/mc-mods/lotmcraft/files/all)
- [Upstream source](https://github.com/jakob-wlf/LOTMCraft-Neoforge)

The JAR must contain Java classes, including
`de/jakob/lotm/util/BeyonderData.class`. A resource-only patch is insufficient.
If a patch is required, install it according to the upstream instructions and
keep the complete mod available for compilation.

The dependency used during development was 114,923,434 bytes with SHA-256:
`D067E05369387D707F3BDFB57BC7F5CF142B96146B876297221A8191BC8F9736`.
This hash documents the inspected artifact; it is not enforced by the build.
