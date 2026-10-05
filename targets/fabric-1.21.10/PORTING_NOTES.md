# Jukebox 360 — Fabric 1.21.10 v1.1.0

- Target: Minecraft 1.21.10 / Fabric Loader 0.18.4 / Java 21.
- Uses Yarn `1.21.10+build.3`.
- Mod version: 1.1.0.
- Replaced the old thread-local sound bridge with a post-play `SoundSystem.sources` lookup, so tagging is queued after vanilla source setup.
- Vanilla/Etched music-disc compatibility remains category-based (`RECORDS`), so correctly categorized custom discs are included without a hard dependency.
- Added optional Simple Voice Chat API integration for Audio Player custom discs. It handles only the `music_discs` OpenAL category and leaves player voice, groups, microphone testing, custom note blocks, and goat horns unchanged.
- The Fabric JSON config already has safe defaults and catches read/write failures; it does not share NeoForge's unbound `ModConfigSpec#get()` startup issue.
- This source package was prepared without running Gradle or compiling it.
