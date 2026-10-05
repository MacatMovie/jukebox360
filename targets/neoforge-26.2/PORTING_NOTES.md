# Jukebox 360 – NeoForge 26.2 port notes

- Target: Minecraft 26.2 / NeoForge 26.2.x
- Mod version: v1.1.0 (shared feature release).
- Replaced the packaged mod icon (`src/main/resources/logo.png`) with the supplied handcrafted 256×256 PNG.
- Audio implementation remains compatible with 26.2: `Channel#setSelfPosition`, the OpenAL source field, and the NeoForge sound source events remain available.
- Minecraft 26.2 still requires Java 25.
