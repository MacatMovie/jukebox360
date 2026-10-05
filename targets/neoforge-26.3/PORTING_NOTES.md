# Jukebox 360 – NeoForge 26.3 port notes

- Target: Minecraft 26.3 / NeoForge 26.3.x beta line.
- Mod version: v1.1.0 (shared feature release).
- Replaced the packaged mod icon (`src/main/resources/logo.png`) with the supplied handcrafted 256×256 PNG.
- The 26.3 audio refactor does not remove the APIs this mod relies on: `SoundEngine#play`, `SoundEngine#instanceToChannel`, `Channel#setSelfPosition`, and the OpenAL source field remain present.
- Minecraft 26.3 still requires Java 25.
