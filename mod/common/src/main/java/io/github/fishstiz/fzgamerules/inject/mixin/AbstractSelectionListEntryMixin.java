package io.github.fishstiz.fzgamerules.inject.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "net.minecraft.client.gui.components.AbstractSelectionList$Entry")
abstract class AbstractSelectionListEntryMixin {
    @Shadow
    public abstract int getY();

    @Shadow
    public abstract int getHeight();

    @Shadow
    public abstract int getX();

    @ModifyReturnValue(method = "isMouseOver", at = @At("RETURN"))
    protected boolean modifyIsMouseOver(boolean original, double mx, double my) {
        return original;
    }
}
