package io.github.fishstiz.fzgamerules.inject;

import io.github.fishstiz.fzgamerules.gui.CategoryList;
import net.minecraft.client.gui.screens.worldselection.AbstractGameRulesScreen;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface RuleListContext {
    default void fzgamerules$refreshEntries() {
    }

    default int fzgamerules$computeRowLeft(int originalRowLeft, int scrollbarWidth) {
        return originalRowLeft - scrollbarWidth - GameRulesScreenContext.fzgamerules$spacing();
    }

    default int fzgamerules$computeRowWidth(AbstractGameRulesScreen.RuleList ruleList) {
        return ruleList.getWidth()
               - GameRulesScreenContext.fzgamerules$resetButtonWidth()
               - GameRulesScreenContext.fzgamerules$spacing() * 3
               - ruleList.scrollbarWidth() * 2;
    }

    default int fzgamerules$computeScrollbarX(AbstractGameRulesScreen.RuleList ruleList) {
        return ruleList.getRight() - ruleList.scrollbarWidth();
    }

    default void fzgamerules$scrollEntryToTop(AbstractGameRulesScreen.RuleList ruleList, AbstractGameRulesScreen.RuleEntry entry) {
        int y = 0;

        for (AbstractGameRulesScreen.RuleEntry child : ruleList.children()) {
            if (child == entry) {
                break;
            }

            y += child.getHeight();
        }

        ruleList.setScrollAmount(y);
    }

    default void fzgamerules$refocusGameRule(
            AbstractGameRulesScreen.RuleList ruleList,
            AbstractGameRulesScreen.@Nullable RuleEntry previousFocused
    ) {
        if (previousFocused instanceof RuleEntryContext ctx) {
            GameRule<?> gameRule = ctx.fzgamerules$getGameRule();
            if (gameRule != null) {
                for (AbstractGameRulesScreen.RuleEntry child : ruleList.children()) {
                    if (((RuleEntryContext) child).fzgamerules$getGameRule() == gameRule) {
                        ruleList.setFocused(child);
                        break;
                    }
                }
            }
        }
    }

    default void fzgamerules$populateCategories(
            AbstractGameRulesScreen.RuleList ruleList,
            @Nullable CategoryList categoryList,
            @Nullable GameRuleCategory visibleCategory
    ) {
        if (categoryList != null) {
            categoryList.populateCategories(
                    ruleList.children().stream()
                            .filter(AbstractGameRulesScreen.CategoryRuleEntry.class::isInstance)
                            .map(AbstractGameRulesScreen.CategoryRuleEntry.class::cast)
                            .toList(),
                    category -> fzgamerules$scrollEntryToTop(ruleList, category)
            );
            categoryList.onViewCategory(visibleCategory);
        }
    }

    default @Nullable GameRuleCategory fzgamerules$getViewedCategory(AbstractGameRulesScreen.RuleList ruleList) {
        int scroll = (int) ruleList.scrollAmount();
        int maxScroll = ruleList.maxScrollAmount();
        List<AbstractGameRulesScreen.RuleEntry> entries = ruleList.children();

        if (scroll <= 1) {
            for (AbstractGameRulesScreen.RuleEntry entry : entries) {
                GameRuleCategory category = ((RuleEntryContext) entry).fzgamerules$getGameRuleCategory();
                if (category != null) {
                    return category;
                }
            }
            return null;
        }

        if (scroll >= maxScroll - 1) {
            for (int i = entries.size() - 1; i >= 0; i--) {
                GameRuleCategory category = ((RuleEntryContext) entries.get(i)).fzgamerules$getGameRuleCategory();
                if (category != null) {
                    return category;
                }
            }
            return null;
        }

        int anchorY = scroll + (int) (ruleList.getHeight() * 0.25);
        GameRuleCategory viewed = null;
        int y = 0;
        for (AbstractGameRulesScreen.RuleEntry entry : entries) {
            if (y > anchorY && viewed != null) {
                return viewed;
            }
            GameRuleCategory category = ((RuleEntryContext) entry).fzgamerules$getGameRuleCategory();
            if (category != null) {
                viewed = category;
            }
            y += entry.getHeight();
        }

        return viewed;
    }
}
