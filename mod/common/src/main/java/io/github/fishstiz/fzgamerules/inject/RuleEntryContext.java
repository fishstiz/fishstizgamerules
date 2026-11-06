package io.github.fishstiz.fzgamerules.inject;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.worldselection.AbstractGameRulesScreen;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRules;
import org.jspecify.annotations.Nullable;

public interface RuleEntryContext {
    default <T> void fzgamerules$setGameRule(GameRule<T> gameRule) {
    }

    default @Nullable GameRule<?> fzgamerules$getGameRule() {
        return null;
    }

    default void fzgamerules$setGameRuleCategory(GameRuleCategory gameRuleCategory) {
    }

    default @Nullable GameRuleCategory fzgamerules$getGameRuleCategory() {
        GameRule<?> gameRule = fzgamerules$getGameRule();
        if (gameRule != null) {
            return gameRule.category();
        }

        return null;
    }

    default @Nullable AbstractWidget fzgamerules$getResetButton() {
        return null;
    }

    default void fzgamerules$extractResetButton(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float a,
            GameRules gameRules,
            AbstractGameRulesScreen.RuleList ruleList,
            AbstractGameRulesScreen.RuleEntry entry
    ) {
        AbstractWidget resetButton = fzgamerules$getResetButton();

        if (resetButton != null) {
            GameRule<?> gameRule = fzgamerules$getGameRule();
            if (gameRule != null) {
                resetButton.active = gameRule.defaultValue() != gameRules.get(gameRule);
            }

            int x = (entry.getContentRight() - ruleList.scrollbarWidth() / 2) + GameRulesScreenContext.fzgamerules$spacing();
            int y = entry.getContentY();

            resetButton.setPosition(x, y);
            resetButton.extractRenderState(graphics, mouseX, mouseY, a);
        }
    }

}
