# Jukebox 360 – Fabric 26.3 port notes

- Target: Minecraft 26.3 / Fabric Loader 0.19.5.
- Uses Fabric Loom 1.17.21.
- Mod version: v1.1.1 (shared feature release).
- Replaced the packaged mod icon (`src/main/resources/logo.png`) with the supplied handcrafted 256×256 PNG.
- Verified against the 26.3 audio API shape: `SoundEngine#play`, `SoundEngine#instanceToChannel`, `Channel#setSelfPosition`, and the OpenAL source field remain available.
- Minecraft 26.3 requires Java 25.
