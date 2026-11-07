package io.github.fishstiz.fzgamerules.gui;

import io.github.fishstiz.fidgetz.v0.gui.components.*;
import io.github.fishstiz.fidgetz.v0.gui.renderables.Renderables;
import io.github.fishstiz.fzgamerules.inject.RuleEntryContext;
import io.github.fishstiz.fzgamerules.inject.mixin.CategoryRuleEntryAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.worldselection.AbstractGameRulesScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class CategoryList extends FZAbstractListWidget<CategoryList.Entry> {
    private static final int HIGHLIGHT_COLOR = 0x26FFFFFF;
    private @Nullable Entry viewedEntry;

    private @Nullable Entry getEntry(@Nullable GameRuleCategory gameRuleCategory) {
        if (gameRuleCategory == null) {
            return null;
        }

        for (Entry entry : children()) {
            if (entry.category == gameRuleCategory) {
                return entry;
            }
        }

        return null;
    }

    public void onViewCategory(@Nullable GameRuleCategory gameRuleCategory) {
        Entry newViewedEntry = getEntry(gameRuleCategory);
        if (newViewedEntry != this.viewedEntry) {
            if (newViewedEntry != null) {
                scrollToEntry(newViewedEntry);
            }

            this.viewedEntry = newViewedEntry;
        }
    }

    public void populateCategories(
            List<AbstractGameRulesScreen.CategoryRuleEntry> categories,
            Consumer<AbstractGameRulesScreen.CategoryRuleEntry> selectHandler
    ) {
        clearEntries();

        for (AbstractGameRulesScreen.RuleEntry ruleEntry : categories) {
            if (ruleEntry instanceof AbstractGameRulesScreen.CategoryRuleEntry categoryEntry) {
                Component label = ((CategoryRuleEntryAccess) categoryEntry).getWidget().getMessage()
                        .plainCopy()
                        .withStyle(ChatFormatting.BOLD);

                GameRuleCategory category = ((RuleEntryContext) categoryEntry).fzgamerules$getGameRuleCategory();
                addEntry(new Entry(category, label, () -> selectHandler.accept(categoryEntry)));
            }
        }

        repositionEntries();
    }

    @Override
    protected int maxContentWidth() {
        return 0;
    }

    public class Entry extends FZAbstractListWidget.Entry {
        private final @Nullable GameRuleCategory category;
        private final AbstractWidget button;

        public Entry(@Nullable GameRuleCategory category, Component label, Runnable onPress) {
            super(24);
            this.category = category;
            this.button = FZButton.builder()
                    .sprites(new WidgetRenderables(Renderables.empty(), Renderables.fill(HIGHLIGHT_COLOR)))
                    .message(label)
                    .leftAlignedMessage()
                    .onPress(onPress)
                    .build();
            fidgetz$setHovered(false);
        }

        @Override
        public void setX(int x) {
            super.setX(x);
            button.setX(x);
        }

        @Override
        public void setY(int y) {
            super.setY(y);
            button.setY(y);
        }

        @Override
        protected void setWidth(int width) {
            super.setWidth(width);
            button.setWidth(width);
        }

        @Override
        protected void setHeight(int height) {
            super.setHeight(height);
            button.setHeight(height);
        }

        @Override
        protected void setBounds(int x, int y, int width, int height) {
            super.setBounds(x, y, width, height);
            button.setRectangle(width, height, x, y);
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            if (viewedEntry == this) {
                graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), HIGHLIGHT_COLOR);
            }

            button.extractRenderState(graphics, mouseX, mouseY, a);
        }

        @Override
        public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent navigationEvent) {
            return ComponentPath.path(this, button.nextFocusPath(navigationEvent));
        }

        @Override
        public @Nullable ComponentPath getCurrentFocusPath() {
            return ComponentPath.path(this, button.getCurrentFocusPath());
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(button);
        }
    }
}