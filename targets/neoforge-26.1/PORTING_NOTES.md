# Jukebox 360 — NeoForge 26.1 v1.1.0

- Concrete development target: Minecraft 26.1.2 / NeoForge 26.1.2.94.
- Metadata accepts the 26.1.x line (`[26.1,26.2)`).
- Java 25 source/toolchain.
- Mod version: 1.1.0.
- Uses NeoForge's normal and streaming sound-source events instead of fragile synthetic `SoundEngine` lambda names.
- Keeps the v1.1.0 safe config accessors: defaults are returned until the client config is bound, preventing the fresh-instance startup crash found in NeoForge 1.21.1 v1.0.7.
- Music-disc compatibility remains category-based (`SoundSource.RECORDS`), including Etched/custom discs that use the records source.
- This source package was prepared without running Gradle or compiling it.

## NeoForge 26.1 mixin remapping note

Minecraft 26.1 is shipped unobfuscated. The Channel and Minecraft mixins therefore explicitly use `remap = false` for their targets, shadows, and injections. This prevents the legacy Mixin annotation processor from trying to generate MCP/SRG obfuscation mappings that do not exist for 26.1.


## NeoForge 26.1 mixin registration fix

- Registers `jukebox_360.mixins.json` through the NeoForge 26.1 `[[mixins]]` TOML block.
- Removes the ineffective `mixinConfigs` property from the `[[mods]]` table and the legacy manifest fallback.
- Replaces the unconditional `ChannelTagAccessor` cast with an `instanceof` guard that logs only once if transformation is unavailable.

- Public build label: `26.1.x`; `26.1.2` remains only the concrete development/compile target.
