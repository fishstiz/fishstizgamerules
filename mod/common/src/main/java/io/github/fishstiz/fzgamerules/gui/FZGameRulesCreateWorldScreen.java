package io.github.fishstiz.fzgamerules.gui;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.fishstiz.fidgetz.v0.gui.components.*;
import io.github.fishstiz.fidgetz.v0.gui.components.events.FZHoverableContainer;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZLayout;
import io.github.fishstiz.fzgamerules.inject.GameRulesScreenContext;
import io.github.fishstiz.fzgamerules.inject.RuleEntryContext;
import io.github.fishstiz.fzgamerules.inject.RuleListContext;
import io.github.fishstiz.fzgamerules.inject.mixin.AbstractGameRulesScreenAccess;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.worldselection.AbstractGameRulesScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationGameRulesScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRules;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class FZGameRulesCreateWorldScreen extends WorldCreationGameRulesScreen implements GameRulesScreenContext, FZHoverableContainer, FZDialogContainer, FZContextMenu.Source {
    private final FZDialogManager dialogManager = new FZDialogManager(this);
    private @Nullable FZLayout layout;
    private @Nullable CategoryList categoryList;

    public FZGameRulesCreateWorldScreen(GameRules gameRules, Consumer<Optional<GameRules>> exitCallback) {
        super(gameRules, exitCallback);
    }

    public static FZGameRulesCreateWorldScreen fromVanilla(WorldCreationGameRulesScreen screen) {
        AbstractGameRulesScreenAccess screenAccess = ((AbstractGameRulesScreenAccess) screen);
        return new FZGameRulesCreateWorldScreen(screenAccess.getGameRules(), screenAccess.getExitCallback());
    }

    @Override
    protected void init() {
        this.layout = fzgamerules$createLayout(this, gameRules);
        this.layout.visitWidgets(this::addRenderableWidget);
        repositionElements();
    }

    @Override
    protected void repositionElements() {
        if (this.layout != null) {
            this.layout.arrangeElements();
            this.layout.fidgetz$setSize(Math.min(GameRulesScreenContext.fzgamerules$maxWidth(), this.width), this.height);
            this.layout.setX(this.width / 2 - this.layout.getWidth() / 2);
            if (this.ruleList != null) {
                this.ruleList.setScrollAmount(this.ruleList.scrollAmount());
            }
        } else {
            rebuildWidgets();
        }
    }

    @Override
    public Optional<AbstractGameRulesScreen.RuleList> fzgamerules$createRuleList(GameRules gameRules) {
        return Optional.of(new RuleList(gameRules));
    }

    @Override
    public void fzgamerules$setSearchBox(FZTextField searchBox) {
        this.searchBox = searchBox;
    }

    @Override
    public void fzgamerules$setCategoryList(CategoryList categoryList) {
        this.categoryList = categoryList;
    }

    @Override
    public void fzgamerules$setDoneButton(Button doneButton) {
        this.doneButton = doneButton;
    }

    @Override
    public void fzgamerules$setRuleList(AbstractGameRulesScreen.RuleList ruleList) {
        if (this.layout != null) {
            this.layout.arrangeElements();
        }

        this.ruleList = ruleList;
        this.ruleList.setScrollAmount(this.ruleList.scrollAmount());
    }

    @Override
    public boolean fzgamerules$shouldIncludeReset() {
        return true;
    }

    @Override
    public List<? extends FZDialog> fidgetz$Dialogs() {
        return dialogManager.dialogs();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (fidgetz$captureEventForDialogs(event)) {
            return true;
        }

        boolean clicked = super.mouseClicked(event, doubleClick);

        double mx = event.x();
        double my = event.y();
        if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT
            && dialogManager.get("ContextMenu").filter(menu -> menu.isMouseOver(mx, my)).isEmpty()) {
            dialogManager.put(FZContextMenu.builder(this)
                    .id("ContextMenu")
                    .focusOnOpen(!clicked)
                    .buildAndOpen(mx, my, fidgetz$collectContextEntries(mx, my)));

            return true;
        }

        return clicked;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        fidgetz$updateHovered(mouseX, mouseY);
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    // its not feasible to completely change rule list because of custom rule entries
    public class RuleList extends AbstractGameRulesScreen.RuleList implements RuleListContext, FZHoverableContainer {
        private @Nullable GameRuleCategory visibleCategory = null;

        public RuleList(GameRules gameRules) {
            super(gameRules);
        }

        @Override
        protected @Nullable RuleEntry getHovered() {
            GuiEventListener trueHovered = fidgetz$getHovered();
            if (trueHovered == null) {
                return null;
            }

            RuleEntry hovered = super.getHovered();
            if (trueHovered == hovered) {
                return hovered;
            }

            return null;
        }

        private void refreshViewedCategory() {
            GameRuleCategory newVisibleCategory = fzgamerules$getViewedCategory(this);
            if (this.visibleCategory != newVisibleCategory) {
                this.visibleCategory = newVisibleCategory;
                if (categoryList != null) {
                    categoryList.onViewCategory(newVisibleCategory);
                }
            }

        }

        @Override
        public void populateChildren(String filter) {
            super.populateChildren(filter);
            if (categoryList != null) {
                categoryList.populateCategories(
                        children().stream()
                                .filter(CategoryRuleEntry.class::isInstance)
                                .map(CategoryRuleEntry.class::cast)
                                .toList(),
                        category -> fzgamerules$scrollEntryToTop(this, category)
                );
                categoryList.onViewCategory(this.visibleCategory);
            }
            refreshViewedCategory();
        }

        @Override
        public void setScrollAmount(double scrollAmount) {
            super.setScrollAmount(scrollAmount);
            // don't check scroll amount because of smooth scrolling mods not using the setter
            refreshViewedCategory();
        }


        @Override
        public int getRowLeft() {
            return fzgamerules$computeRowLeft(super.getRowLeft(), scrollbarWidth());
        }

        @Override
        public int getRowWidth() {
            return fzgamerules$computeRowWidth(this);
        }

        @Override
        public int scrollBarX() {
            return fzgamerules$computeScrollbarX(this);
        }

        @Override
        protected void extractItem(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, RuleEntry entry) {
            super.extractItem(graphics, mouseX, mouseY, a, entry);
            ((RuleEntryContext) entry).fzgamerules$extractResetButton(graphics, mouseX, mouseY, a, gameRules, this, entry);
        }
    }
}
