package com.macatmovie.jukebox360.mixin;

import com.macatmovie.jukebox360.Jukebox360Runtime;
import com.macatmovie.jukebox360.config.ClientConfig;
import com.macatmovie.jukebox360.mixinaccess.SourceTagAccessor;
import com.mojang.logging.LogUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.Source;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.openal.AL10;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Source.class)
public abstract class SourceMixin implements SourceTagAccessor {
    private static final Logger LOG = LogUtils.getLogger();

    @Shadow @Final
    private int pointer;

    @Unique private boolean jukebox360$records;
    @Unique private boolean jukebox360$affected;
    @Unique private Vec3d jukebox360$worldPos;

    @Override public void jukebox360$setRecords(boolean records) { this.jukebox360$records = records; }
    @Override public boolean jukebox360$isRecords() { return this.jukebox360$records; }
    @Override public void jukebox360$setAffected(boolean affected) { this.jukebox360$affected = affected; }
    @Override public boolean jukebox360$isAffected() { return this.jukebox360$affected; }
    @Override public void jukebox360$setWorldPos(Vec3d pos) { this.jukebox360$worldPos = pos; }
    @Override public Vec3d jukebox360$getWorldPos() { return this.jukebox360$worldPos; }
    @Override public int jukebox360$getSourceId() { return this.pointer; }
    @Override public void jukebox360$applyProjectionTick() { jukebox360$applyProjection(); }

    @Unique
    private void jukebox360$applyProjection() {
        if (!this.jukebox360$affected || this.jukebox360$worldPos == null) return;

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        Vec3d ear = player.getEyePos();
        Vec3d toWorld = this.jukebox360$worldPos.subtract(ear);
        double distance = toWorld.length();
        if (distance < 1.0E-6D) return;

        Vec3d worldDirection = toWorld.normalize();
        Vec3d lookDirection = player.getRotationVec(1.0F);
        if (lookDirection.lengthSquared() < 1.0E-6D) lookDirection = new Vec3d(0.0D, 0.0D, 1.0D);
        lookDirection = lookDirection.normalize();

        double blend = ClientConfig.blend();
        Vec3d blendedDirection = worldDirection.multiply(1.0D - blend).add(lookDirection.multiply(blend));
        if (blendedDirection.lengthSquared() < 1.0E-10D) blendedDirection = worldDirection;

        Vec3d output = ear.add(blendedDirection.normalize().multiply(distance));
        AL10.alSourcei(this.pointer, AL10.AL_SOURCE_RELATIVE, AL10.AL_FALSE);
        AL10.alSource3f(this.pointer, AL10.AL_POSITION, (float) output.x, (float) output.y, (float) output.z);

        if (ClientConfig.debugLog()) {
            LOG.info("[Jukebox360] Projected source {} ({}) with blend {}", this.pointer,
                    this.jukebox360$records ? "RECORDS" : "NOTE_BLOCK", blend);
        }
    }

    @Inject(method = "setPosition", at = @At("TAIL"))
    private void jukebox360$afterSetPosition(Vec3d position, CallbackInfo ci) {
        if (this.jukebox360$affected) {
            this.jukebox360$worldPos = position;
            this.jukebox360$applyProjection();
        }
    }

    @Inject(method = "stop", at = @At("HEAD"))
    private void jukebox360$stop(CallbackInfo ci) {
        Jukebox360Runtime.remove(this);
        this.jukebox360$records = false;
        this.jukebox360$affected = false;
        this.jukebox360$worldPos = null;
    }
}
