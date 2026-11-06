package com.kawan.lotmanchors.mixin;

import de.jakob.lotm.events.HonorificNamesEventHandler;
import com.kawan.lotmanchors.event.HonorificPrayerHandler;
import net.minecraft.server.level.ServerPlayer;
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
            LivingEntity worshipper,
            LivingEntity target,
            CallbackInfo ci) {

        if (target instanceof ServerPlayer recipient && worshipper instanceof ServerPlayer prayingPlayer) {
            HonorificPrayerHandler.onPrayer(recipient, prayingPlayer);
        }
    }
}
