package io.github.fishstiz.fzgamerules.inject.mixin;

import net.minecraft.client.gui.components.FocusableTextWidget;
import net.minecraft.client.gui.screens.worldselection.AbstractGameRulesScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractGameRulesScreen.CategoryRuleEntry.class)
public interface CategoryRuleEntryAccess {
    @Accessor
    FocusableTextWidget getWidget();
}
