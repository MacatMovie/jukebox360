package com.macatmovie.jukebox360.mixin;

import com.macatmovie.jukebox360.config.ClientConfig;
import com.macatmovie.jukebox360.mixinaccess.ChannelTagAccessor;
import com.mojang.blaze3d.audio.Channel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.openal.AL10;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Channel.class)
public class ChannelOpenALMixin implements ChannelTagAccessor {
    private static final Logger LOG = LogManager.getLogger("Jukebox360");

    @Shadow(remap = true) private int source;

    @Unique private boolean jukebox360$records = false;
    @Unique private boolean jukebox360$affected = false;
    @Unique private Vec3 jukebox360$worldPos = null;

    @Override public void jukebox360$setRecords(boolean records) { this.jukebox360$records = records; }
    @Override public boolean jukebox360$isRecords() { return this.jukebox360$records; }

    @Override public void jukebox360$setAffected(boolean affected) { this.jukebox360$affected = affected; }
    @Override public boolean jukebox360$isAffected() { return this.jukebox360$affected; }

    @Override public void jukebox360$setWorldPos(Vec3 pos) { this.jukebox360$worldPos = pos; }
    @Override public Vec3 jukebox360$getWorldPos() { return this.jukebox360$worldPos; }

    @Override public int jukebox360$getSourceId() { return this.source; }

    private void applyProjection() {
        if (!jukebox360$affected || jukebox360$worldPos == null) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        Vec3 ear = player.getEyePosition();
        Vec3 toWorld = jukebox360$worldPos.subtract(ear);
        double dist = toWorld.length();
        if (dist < 1e-6) return;

        Vec3 dirWorld = toWorld.normalize();

        Vec3 look = player.getLookAngle();
        if (look.lengthSqr() < 1e-6) look = new Vec3(0, 0, 1);
        Vec3 dirLook = look.normalize();

        double blend = ClientConfig.BLEND.get();

        Vec3 blendedDir = dirWorld.scale(1.0 - blend).add(dirLook.scale(blend));
        if (blendedDir.lengthSqr() < 1e-10) blendedDir = dirWorld;
        blendedDir = blendedDir.normalize();

        Vec3 out = ear.add(blendedDir.scale(dist));

        AL10.alSourcei(this.source, AL10.AL_SOURCE_RELATIVE, AL10.AL_FALSE);
        AL10.alSource3f(this.source, AL10.AL_POSITION, (float) out.x, (float) out.y, (float) out.z);

        if (ClientConfig.DEBUG_LOG.get()) {
            LOG.info("[Jukebox360] Project srcId={} kind={} blend={} dist={}",
                    this.source,
                    this.jukebox360$records ? "RECORDS" : "NOTE_BLOCK",
                    String.format("%.2f", blend),
                    String.format("%.2f", dist));
        }
    }

    @Inject(method = "m_83652_(I)V", at = @At("TAIL"), remap = false, require = 0)
    private void jukebox360$after_m83652(int v, CallbackInfo ci) {
        applyProjection();
    }

    @Inject(method = "m_83654_(Lnet/minecraft/world/phys/Vec3;)V", at = @At("TAIL"), remap = false, require = 0)
    private void jukebox360$after_m83654(Vec3 pos, CallbackInfo ci) {
        if (jukebox360$affected) {
            this.jukebox360$worldPos = pos;
            // Apply immediately for short sounds (note blocks).
            applyProjection();
        }
    }

        @Inject(method = "m_83666_(F)V", at = @At("TAIL"), remap = false, require = 0)
    private void jukebox360$after_m83666(float v, CallbackInfo ci) {
        // Some short sounds (note blocks) may only get a couple of updates; apply immediately after gain is set.
        applyProjection();
    }

@Inject(method = "stop", at = @At("HEAD"), remap = false, require = 0)
    private void jukebox360$stop(CallbackInfo ci) {
        jukebox360$records = false;
        jukebox360$affected = false;
        jukebox360$worldPos = null;
    }
}
