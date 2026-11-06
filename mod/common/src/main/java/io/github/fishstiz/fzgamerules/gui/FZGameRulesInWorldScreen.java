package io.github.fishstiz.fzgamerules.gui;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.fishstiz.fidgetz.v0.gui.components.*;
import io.github.fishstiz.fidgetz.v0.gui.components.events.FZHoverableContainer;
import io.github.fishstiz.fidgetz.v0.gui.components.events.FZHoverableElement;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZLayout;
import io.github.fishstiz.fzgamerules.inject.GameRulesScreenContext;
import io.github.fishstiz.fzgamerules.inject.RuleEntryContext;
import io.github.fishstiz.fzgamerules.inject.RuleListContext;
import io.github.fishstiz.fzgamerules.inject.mixin.InWorldGameRulesScreenAccess;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.LoadingDotsWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.InWorldGameRulesScreen;
import net.minecraft.client.gui.screens.worldselection.AbstractGameRulesScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRules;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class FZGameRulesInWorldScreen extends InWorldGameRulesScreen implements GameRulesScreenContext, FZHoverableContainer, FZDialogContainer, FZContextMenu.Source {
    protected final FZDialogManager dialogManager = new FZDialogManager(this);
    private @Nullable FZLayout layout;
    private @Nullable CategoryList categoryList;

    public FZGameRulesInWorldScreen(
            ClientPacketListener connection,
            Consumer<Optional<GameRules>> exitCallback,
            Screen lastScreen
    ) {
        super(connection, exitCallback, lastScreen);
    }

    public static FZGameRulesInWorldScreen fromVanilla(InWorldGameRulesScreen inWorldGameRulesScreen) {
        InWorldGameRulesScreenAccess screenAccess = (InWorldGameRulesScreenAccess) inWorldGameRulesScreen;
        return new FZGameRulesInWorldScreen(
                screenAccess.getConnection(),
                screenAccess.getExitCallback(),
                screenAccess.getLastScreen()
        );
    }

    @Override
    protected void init() {
        super.initContent();
        this.layout = fzgamerules$createLayout(this, new GameRules(List.of()));
        this.layout.visitWidgets(this::addRenderableWidget);

        LoadingDotsWidget loadingDotsWidget = getLoadingDotsWidget();
        if (loadingDotsWidget != null) {
            addRenderableOnly(loadingDotsWidget);
        }

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

    private @Nullable LoadingDotsWidget getLoadingDotsWidget() {
        return ((InWorldGameRulesScreenAccess) this).getLoadingDotsWidget();
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

        if (this.ruleList != null) {
            ruleList.setRectangle(this.ruleList.getWidth(), this.ruleList.getHeight(), this.ruleList.getX(), this.ruleList.getY());
            removeWidget(this.ruleList);
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

    public class RuleList extends AbstractGameRulesScreen.RuleList implements RuleListContext, FZHoverableContainer {
        private @Nullable GameRuleCategory visibleCategory = null;

        public RuleList(GameRules gameRules) {
            super(gameRules);
        }

        @Override
        protected int addEntry(RuleEntry entry, int height) {
            if (entry instanceof FZHoverableElement hoverable) {
                hoverable.fidgetz$setHovered(false);
            }
            return super.addEntry(entry, height);
        }

        @Override
        protected void addEntryToTop(RuleEntry entry, int height) {
            super.addEntryToTop(entry, height);
            if (entry instanceof FZHoverableElement hoverable) {
                hoverable.fidgetz$setHovered(false);
            }
        }

        @Override
        protected @Nullable RuleEntry getHovered() {
            GuiEventListener trueHovered = fidgetz$getHovered();
            if (trueHovered instanceof RuleEntry entry) {
                return entry;
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
        protected void clearEntries() {
            super.clearEntries();
            setFocused(null);
            fidgetz$setHovered(null);
        }

        @Override
        public void populateChildren(String filter) {
            RuleEntry previousFocused = getFocused();
            RuleEntry previousHovered = getHovered();
            super.populateChildren(filter);
            fzgamerules$populateCategories(this, categoryList, this.visibleCategory);
            fzgamerules$restoreGameRuleUiState(this, previousFocused, previousHovered);
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
        public void setX(int x) {
            super.setX(x);

            AbstractGameRulesScreen.RuleList canonicalList = FZGameRulesInWorldScreen.this.ruleList;
            if (canonicalList != null && canonicalList != this) {
                canonicalList.setX(x);
            }

            LoadingDotsWidget loadingDotsWidget = getLoadingDotsWidget();
            if (loadingDotsWidget != null) {
                loadingDotsWidget.setX(x);
            }
        }

        @Override
        public void setY(int y) {
            super.setY(y);

            AbstractGameRulesScreen.RuleList canonicalList = FZGameRulesInWorldScreen.this.ruleList;
            if (canonicalList != null && canonicalList != this) {
                canonicalList.setY(y);
            }

            LoadingDotsWidget loadingDotsWidget = getLoadingDotsWidget();
            if (loadingDotsWidget != null) {
                loadingDotsWidget.setY(y + getHeight() - getHeight() / 2 - loadingDotsWidget.getHeight() / 2);
            }
        }

        @Override
        public void setWidth(int width) {
            super.setWidth(width);

            AbstractGameRulesScreen.RuleList canonicalList = FZGameRulesInWorldScreen.this.ruleList;
            if (canonicalList != null && canonicalList != this) {
                canonicalList.setWidth(width);
            }

            LoadingDotsWidget loadingDotsWidget = getLoadingDotsWidget();
            if (loadingDotsWidget != null) {
                loadingDotsWidget.setWidth(width);
            }
        }

        @Override
        public void setHeight(int height) {
            super.setHeight(height);

            AbstractGameRulesScreen.RuleList canonicalList = FZGameRulesInWorldScreen.this.ruleList;
            if (canonicalList != null && canonicalList != this) {
                canonicalList.setHeight(height);
            }

            LoadingDotsWidget loadingDotsWidget = getLoadingDotsWidget();
            if (loadingDotsWidget != null) {
                loadingDotsWidget.setY(getY() + height - height / 2 - loadingDotsWidget.getHeight() / 2);
            }
        }

        @Override
        public void setSize(int width, int height) {
            super.setSize(width, height);

            AbstractGameRulesScreen.RuleList canonicalList = FZGameRulesInWorldScreen.this.ruleList;
            if (canonicalList != null && canonicalList != this) {
                canonicalList.setSize(width, height);
            }

            LoadingDotsWidget loadingDotsWidget = getLoadingDotsWidget();
            if (loadingDotsWidget != null) {
                loadingDotsWidget.setWidth(width);
                loadingDotsWidget.setY(getY() + height - height / 2 - loadingDotsWidget.getHeight() / 2);
            }
        }

        @Override
        protected void extractItem(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, RuleEntry entry) {
            RuleEntryContext ctx = (RuleEntryContext) entry;
            ctx.fzgamerules$extractHoverBackground(graphics, this, getHovered(), entry);
            super.extractItem(graphics, mouseX, mouseY, a, entry);
            ctx.fzgamerules$extractResetButton(graphics, mouseX, mouseY, a, gameRules, this, entry);

        }
    }
}
