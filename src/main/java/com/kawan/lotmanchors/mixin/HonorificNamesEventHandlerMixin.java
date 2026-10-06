package com.kawan.lotmanchors.mixin;

import de.jakob.lotm.events.HonorificNamesEventHandler;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HonorificNamesEventHandler.class)
public abstract class HonorificNamesEventHandlerMixin {

    @Inject(
            method = "storePendingPrayer",
            at = @At("TAIL")
    )
    private static void lotmAnchors$onPrayerCompleted(
            LivingEntity target,
            LivingEntity worshipper,
            CallbackInfo ci) {

        System.out.println(
                "[LOTM Anchors] Prayer detected: "
                        + worshipper.getName().getString()
                        + " -> "
                        + target.getName().getString()
        );
    }
}