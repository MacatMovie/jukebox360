package com.macatmovie.jukebox360.compat.voicechat;

import com.macatmovie.jukebox360.config.ClientConfig;
import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.OpenALSoundEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
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

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        Vec3d ear = player.getEyePos();
        Vec3d worldPosition = new Vec3d(position.getX(), position.getY(), position.getZ());
        Vec3d toWorld = worldPosition.subtract(ear);
        double distance = toWorld.length();
        if (distance < 1.0E-6D) return;

        Vec3d worldDirection = toWorld.normalize();
        Vec3d lookDirection = player.getRotationVec(1.0F);
        if (lookDirection.lengthSquared() < 1.0E-6D) {
            lookDirection = new Vec3d(0.0D, 0.0D, 1.0D);
        }
        lookDirection = lookDirection.normalize();

        double blend = ClientConfig.blend();
        Vec3d blendedDirection = worldDirection.multiply(1.0D - blend).add(lookDirection.multiply(blend));
        if (blendedDirection.lengthSquared() < 1.0E-10D) {
            blendedDirection = worldDirection;
        }

        Vec3d output = ear.add(blendedDirection.normalize().multiply(distance));
        AL10.alSourcei(source, AL10.AL_SOURCE_RELATIVE, AL10.AL_FALSE);
        AL10.alSource3f(source, AL10.AL_POSITION, (float) output.x, (float) output.y, (float) output.z);
    }
}
