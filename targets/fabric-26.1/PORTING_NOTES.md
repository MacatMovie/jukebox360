# Jukebox 360 — Fabric 26.1 v1.1.0

- Concrete development target: Minecraft 26.1.2.
- Metadata accepts the 26.1.x line (`>=26.1 <26.2`).
- Uses unobfuscated Mojang names, Fabric Loom's non-remapping plugin, and Java 25.
- Mod version: 1.1.0.
- Audio sources are tagged through `SoundEngine.instanceToChannel` after `play`, avoiding synthetic-lambda and thread-local hooks.
- Vanilla/Etched music-disc compatibility remains category-based (`SoundSource.RECORDS`), including custom discs that use the records source.
- Added optional Simple Voice Chat API integration for Audio Player custom discs. It handles only the `music_discs` OpenAL category and leaves player voice, groups, microphone testing, custom note blocks, and goat horns unchanged.
- This source package was prepared without running Gradle or compiling it.

- Public build label: `26.1.x`; `26.1.2` remains only the concrete development/compile target.
