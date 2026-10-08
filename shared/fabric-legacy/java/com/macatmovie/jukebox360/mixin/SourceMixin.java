package com.macatmovie.jukebox360.mixin;

import com.macatmovie.jukebox360.Jukebox360Runtime;
import com.macatmovie.jukebox360.config.ClientConfig;
import com.macatmovie.jukebox360.mixinaccess.SourceTagAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.Source;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.openal.AL10;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Source.class)
public class SourceMixin implements SourceTagAccessor {
    private static final Logger LOG = LogManager.getLogger("Jukebox360");

    @Shadow private int pointer;

    @Unique private boolean jukebox360$records = false;
    @Unique private boolean jukebox360$affected = false;
    @Unique private Vec3d jukebox360$worldPos = null;

    @Unique private static final double jukebox360$SMOOTHING_TIME_SECONDS = 0.075;
    @Unique private Vec3d jukebox360$smoothedProjectedPos = null;
    @Unique private long jukebox360$lastProjectionNanos = 0L;

    @Override public void jukebox360$setRecords(boolean records) { this.jukebox360$records = records; }
    @Override public boolean jukebox360$isRecords() { return this.jukebox360$records; }

    @Override public void jukebox360$setAffected(boolean affected) {
        this.jukebox360$affected = affected;
        if (!affected) jukebox360$resetSmoothing();
    }
    @Override public boolean jukebox360$isAffected() { return this.jukebox360$affected; }

    @Override public void jukebox360$setWorldPos(Vec3d pos) { this.jukebox360$worldPos = pos; }
    @Override public Vec3d jukebox360$getWorldPos() { return this.jukebox360$worldPos; }

    @Override public int jukebox360$getSourceId() { return this.pointer; }

    @Override public void jukebox360$applyProjectionTick() { applyProjection(); }

    private void applyProjection() {
        if (!jukebox360$affected || jukebox360$worldPos == null) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null) return;

        Vec3d ear = player.getEyePos();
        Vec3d toWorld = jukebox360$worldPos.subtract(ear);
        double dist = toWorld.length();
        if (dist < 1e-6) return;

        Vec3d dirWorld = toWorld.normalize();

        Vec3d look = player.getRotationVec(1.0F);
        if (look.lengthSquared() < 1e-6) look = new Vec3d(0, 0, 1);
        Vec3d dirLook = look.normalize();

        double blend = ClientConfig.BLEND;
        Vec3d blendedDir = dirWorld.multiply(1.0 - blend).add(dirLook.multiply(blend));
        if (blendedDir.lengthSquared() < 1e-10) blendedDir = dirWorld;
        blendedDir = blendedDir.normalize();

        Vec3d out = ear.add(blendedDir.multiply(dist));

        out = jukebox360$smoothProjectedPosition(out);

        AL10.alSourcei(this.pointer, AL10.AL_SOURCE_RELATIVE, AL10.AL_FALSE);
        AL10.alSource3f(this.pointer, AL10.AL_POSITION, (float) out.x, (float) out.y, (float) out.z);

        if (ClientConfig.DEBUG_LOG) {
            LOG.info("[Jukebox360] Project srcId={} kind={} blend={} dist={}",
                    this.pointer,
                    this.jukebox360$records ? "RECORDS" : "NOTE_BLOCK",
                    String.format("%.2f", blend),
                    String.format("%.2f", dist));
        }
    }

    @Unique
    private Vec3d jukebox360$smoothProjectedPosition(Vec3d target) {
        long now = System.nanoTime();

        // First update (or a long pause/source reuse) should snap immediately rather
        // than spending several ticks catching up from an obsolete position.
        if (this.jukebox360$smoothedProjectedPos == null || this.jukebox360$lastProjectionNanos == 0L) {
            this.jukebox360$smoothedProjectedPos = target;
            this.jukebox360$lastProjectionNanos = now;
            return target;
        }

        double dt = (now - this.jukebox360$lastProjectionNanos) / 1_000_000_000.0;
        this.jukebox360$lastProjectionNanos = now;

        if (dt <= 0.0 || dt > 0.5) {
            this.jukebox360$smoothedProjectedPos = target;
            return target;
        }

        // Exponential smoothing is frame-rate/tick-rate independent. With the
        // current 20 Hz update path and a 75 ms time constant, each normal tick
        // moves about 49% toward the new projected position.
        double alpha = 1.0 - Math.exp(-dt / jukebox360$SMOOTHING_TIME_SECONDS);
        alpha = Math.max(0.0, Math.min(1.0, alpha));

        Vec3d current = this.jukebox360$smoothedProjectedPos;
        Vec3d smoothed = current.add(target.subtract(current).multiply(alpha));
        this.jukebox360$smoothedProjectedPos = smoothed;
        return smoothed;
    }

    @Unique
    private void jukebox360$resetSmoothing() {
        this.jukebox360$smoothedProjectedPos = null;
        this.jukebox360$lastProjectionNanos = 0L;
    }

    @Inject(method = "stop", at = @At("HEAD"))
    private void jukebox360$stop(CallbackInfo ci) {
        Jukebox360Runtime.remove(this);
        this.jukebox360$affected = false;
        this.jukebox360$records = false;
        this.jukebox360$worldPos = null;
        jukebox360$resetSmoothing();
    }
}
