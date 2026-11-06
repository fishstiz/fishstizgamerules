package io.github.fishstiz.fzgamerules.inject.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.client.gui.components.AbstractSelectionList$Entry")
abstract class AbstractSelectionListEntryMixin {
    @ModifyReturnValue(method = "isMouseOver", at = @At("RETURN"))
    protected boolean modifyIsMouseOver(boolean original, double mx, double my) {
        return original;
    }
}
