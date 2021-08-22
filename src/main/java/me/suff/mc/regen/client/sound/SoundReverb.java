package me.suff.mc.regen.client.sound;

import me.suff.mc.regen.Regeneration;
import me.suff.mc.regen.common.regen.IRegen;
import me.suff.mc.regen.common.regen.RegenCap;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.EXTEfx;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class SoundReverb {
    private static final Minecraft MC = Minecraft.getInstance();

    private static boolean available;
    private static boolean setup;

    private static int auxEffectSlot;

    public static void addReloader() {
        ((ReloadableResourceManager) MC.getResourceManager()).registerReloadListener((p_10638_, p_10639_, p_10640_, p_10641_, p_10642_, p_10643_) -> {
            setup = false;
            return new CompletableFuture<>();
        });
    }

    public static void setSelfPosition(int soundId) {
        if (!setup) {
            setupEffects();
            setup = true;
        }

        if (available && shouldEcho()) {
            AL11.alSource3i(soundId, EXTEfx.AL_AUXILIARY_SEND_FILTER, auxEffectSlot, 0, EXTEfx.AL_FILTER_NULL);
        }
    }

    private static void setupEffects() {
        available = AL.getCapabilities().ALC_EXT_EFX;
        if (!available) {
            Regeneration.LOG.warn("Unable to setup reverb effects, AL EFX not supported!");
            return;
        }

        auxEffectSlot = EXTEfx.alGenAuxiliaryEffectSlots();
        EXTEfx.alAuxiliaryEffectSloti(auxEffectSlot, EXTEfx.AL_EFFECTSLOT_AUXILIARY_SEND_AUTO, AL10.AL_TRUE);

        int reverbEffectSlot = EXTEfx.alGenEffects();

        EXTEfx.alEffecti(reverbEffectSlot, EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_EAXREVERB);
        EXTEfx.alEffectf(reverbEffectSlot, EXTEfx.AL_EAXREVERB_DECAY_TIME, 9F);

        EXTEfx.alAuxiliaryEffectSloti(auxEffectSlot, EXTEfx.AL_EFFECTSLOT_EFFECT, reverbEffectSlot);
    }

    private static boolean shouldEcho() {
        if (Minecraft.getInstance().level == null) return false;
        IRegen data = RegenCap.get(Minecraft.getInstance().player).orElse(null);
        if (data != null) {
            return data.regenState().isGraceful();
        }
        return false;
    }

}
