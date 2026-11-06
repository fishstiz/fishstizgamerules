package io.github.fishstiz.fzgamerules.inject.mixin;

import net.minecraft.client.gui.screens.worldselection.AbstractGameRulesScreen;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractGameRulesScreen.CategoryRuleEntry.class)
abstract class CategoryRuleEntryMixin extends RuleEntryMixin {
    @Unique
    @Nullable
    GameRuleCategory fzgamerules$gameRuleCategory;

    @Override
    public void fzgamerules$setGameRuleCategory(GameRuleCategory gameRuleCategory) {
        this.fzgamerules$gameRuleCategory = gameRuleCategory;
    }

    @Override
    public @Nullable GameRuleCategory fzgamerules$getGameRuleCategory() {
        return fzgamerules$gameRuleCategory;
    }
}
