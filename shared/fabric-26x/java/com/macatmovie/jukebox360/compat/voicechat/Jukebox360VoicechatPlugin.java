package com.macatmovie.jukebox360.compat.voicechat;

import com.macatmovie.jukebox360.config.ClientConfig;
import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.OpenALSoundEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.openal.AL10;

/**
 * Optional Simple Voice Chat integration used by Audio Player custom music discs.
 * The handler is deliberately limited to Audio Player's "music_discs" category,
 * so proximity voice, groups, microphone testing, note blocks, and goat horns are
 * not modified.
 */
public final class Jukebox360VoicechatPlugin implements VoicechatPlugin {
    private static final String MUSIC_DISC_CATEGORY = "music_discs";

    @Override
    public String getPluginId() {
        return "jukebox_360";
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(OpenALSoundEvent.Post.class, this::onOpenALSound);
    }

    private void onOpenALSound(OpenALSoundEvent.Post event) {
        if (!MUSIC_DISC_CATEGORY.equals(event.getCategory())) return;

        Position position = event.getPosition();
        if (position == null) return;

        int source = event.getSource();
        if (source <= 0 || !AL10.alIsSource(source)) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        Vec3 ear = player.getEyePosition();
        Vec3 worldPosition = new Vec3(position.getX(), position.getY(), position.getZ());
        Vec3 toWorld = worldPosition.subtract(ear);
        double distance = toWorld.length();
        if (distance < 1.0E-6D) return;

        Vec3 worldDirection = toWorld.normalize();
        Vec3 lookDirection = player.getLookAngle();
        if (lookDirection.lengthSqr() < 1.0E-6D) {
            lookDirection = new Vec3(0.0D, 0.0D, 1.0D);
        }
        lookDirection = lookDirection.normalize();

        double blend = ClientConfig.blend();
        Vec3 blendedDirection = worldDirection.scale(1.0D - blend).add(lookDirection.scale(blend));
        if (blendedDirection.lengthSqr() < 1.0E-10D) {
            blendedDirection = worldDirection;
        }

        Vec3 output = ear.add(blendedDirection.normalize().scale(distance));
        AL10.alSourcei(source, AL10.AL_SOURCE_RELATIVE, AL10.AL_FALSE);
        AL10.alSource3f(source, AL10.AL_POSITION, (float) output.x, (float) output.y, (float) output.z);
    }
}
