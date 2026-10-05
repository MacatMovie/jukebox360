package com.macatmovie.jukebox360.mixin;

import com.macatmovie.jukebox360.Jukebox360Runtime;
import com.macatmovie.jukebox360.config.ClientConfig;
import com.macatmovie.jukebox360.mixinaccess.ChannelTagAccessor;
import com.mojang.blaze3d.audio.Channel;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.openal.AL10;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Channel.class, remap = false)
public abstract class ChannelMixin implements ChannelTagAccessor {
    private static final Logger LOG = LogUtils.getLogger();

    @Shadow(remap = false) @Final
    private int source;

    @Unique private boolean jukebox360$records;
    @Unique private boolean jukebox360$affected;
    @Unique private Vec3 jukebox360$worldPos;

    @Override public void jukebox360$setRecords(boolean records) { this.jukebox360$records = records; }
    @Override public boolean jukebox360$isRecords() { return this.jukebox360$records; }
    @Override public void jukebox360$setAffected(boolean affected) { this.jukebox360$affected = affected; }
    @Override public boolean jukebox360$isAffected() { return this.jukebox360$affected; }
    @Override public void jukebox360$setWorldPos(Vec3 pos) { this.jukebox360$worldPos = pos; }
    @Override public Vec3 jukebox360$getWorldPos() { return this.jukebox360$worldPos; }
    @Override public int jukebox360$getSourceId() { return this.source; }
    @Override public void jukebox360$applyProjectionTick() { jukebox360$applyProjection(); }

    @Unique
    private void jukebox360$applyProjection() {
        if (!this.jukebox360$affected || this.jukebox360$worldPos == null) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        Vec3 ear = player.getEyePosition();
        Vec3 toWorld = this.jukebox360$worldPos.subtract(ear);
        double distance = toWorld.length();
        if (distance < 1.0E-6D) return;

        Vec3 worldDirection = toWorld.normalize();
        Vec3 lookDirection = player.getLookAngle();
        if (lookDirection.lengthSqr() < 1.0E-6D) lookDirection = new Vec3(0.0D, 0.0D, 1.0D);
        lookDirection = lookDirection.normalize();

        double blend = ClientConfig.blend();
        Vec3 blendedDirection = worldDirection.scale(1.0D - blend).add(lookDirection.scale(blend));
        if (blendedDirection.lengthSqr() < 1.0E-10D) blendedDirection = worldDirection;

        Vec3 output = ear.add(blendedDirection.normalize().scale(distance));
        AL10.alSourcei(this.source, AL10.AL_SOURCE_RELATIVE, AL10.AL_FALSE);
        AL10.alSource3f(this.source, AL10.AL_POSITION, (float) output.x, (float) output.y, (float) output.z);

        if (ClientConfig.debugLog()) {
            LOG.info("[Jukebox360] Projected source {} ({}) with blend {}", this.source,
                    this.jukebox360$records ? "RECORDS" : "NOTE_BLOCK", blend);
        }
    }

    @Inject(method = "setSelfPosition", at = @At("TAIL"), remap = false)
    private void jukebox360$afterSetSelfPosition(Vec3 position, CallbackInfo ci) {
        if (this.jukebox360$affected) {
            this.jukebox360$worldPos = position;
            this.jukebox360$applyProjection();
        }
    }

    @Inject(method = "stop", at = @At("HEAD"), remap = false)
    private void jukebox360$stop(CallbackInfo ci) {
        Jukebox360Runtime.remove(this);
        this.jukebox360$records = false;
        this.jukebox360$affected = false;
        this.jukebox360$worldPos = null;
    }
}
