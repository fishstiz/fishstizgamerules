package io.github.fishstiz.fzgamerules.inject.mixin;

import io.github.fishstiz.fidgetz.v0.gui.components.FZIconButton;
import io.github.fishstiz.fidgetz.v0.gui.components.WidgetElements;
import io.github.fishstiz.fidgetz.v0.gui.components.events.FZHoverableElement;
import io.github.fishstiz.fidgetz.v0.gui.renderables.Renderables;
import io.github.fishstiz.fzgamerules.FZGameRules;
import io.github.fishstiz.fzgamerules.inject.GameRulesScreenContext;
import io.github.fishstiz.fzgamerules.inject.RuleListContext;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.worldselection.AbstractGameRulesScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.world.level.gamerules.GameRule;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(AbstractGameRulesScreen.GameRuleEntry.class)
abstract class GameRuleEntryMixin extends RuleEntryMixin {
    @Unique
    private static final WidgetElements UNDO_SPRITES;

    static {
        Identifier undoSprite = FZGameRules.id("icon/arrow_u_turn_up_left");
        UNDO_SPRITES = WidgetElements.noFocus(
                Renderables.sprite(undoSprite),
                Renderables.sprite(undoSprite, CommonColors.GRAY),
                16,
                16
        );
    }

    @Shadow
    @Final
    AbstractGameRulesScreen this$0;

    @Shadow
    @Final
    protected List<AbstractWidget> children;

    @Unique
    @Nullable
    private AbstractWidget fzgamerules$resetButton;


    @Override
    public @Nullable AbstractWidget fzgamerules$getResetButton() {
        return this.fzgamerules$resetButton;
    }

    @Override
    public <T> void fzgamerules$setGameRule(GameRule<T> gameRule) {
        super.fzgamerules$setGameRule(gameRule);

        if (gameRule != null && ((GameRulesScreenContext) this$0).fzgamerules$shouldIncludeReset()) {
            this.fzgamerules$resetButton = FZIconButton.builder()
                    .square()
                    .message(Component.translatable("controls.reset"))
                    .icon(UNDO_SPRITES)
                    .onPress(() -> {
                        AbstractGameRulesScreenAccess screenAccess = (AbstractGameRulesScreenAccess) this$0;
                        screenAccess.getGameRules().set(gameRule, gameRule.defaultValue(), null);
                        if (screenAccess.getRuleList() instanceof RuleListContext ruleList) {
                            ruleList.fzgamerules$refreshEntries();
                        }
                    })
                    .build();

            //noinspection ConstantValue
            if (this.fzgamerules$resetButton instanceof FZHoverableElement hoverable) {
                hoverable.fidgetz$setHovered(false);
            }

            children.add(this.fzgamerules$resetButton);
        } else {
            this.fzgamerules$resetButton = null;
        }
    }

    @Unique
    private boolean fzgamerules$isMouseOverWithSlack(double mx, double my) {
        if (this.fzgamerules$resetButton == null
            || !this.fzgamerules$resetButton.visible
            || my < getY()
            || my > getY() + getHeight()) {
            return false;
        }

        AbstractGameRulesScreen.RuleList ruleList = ((AbstractGameRulesScreenAccess) this$0).getRuleList();
        if (ruleList != null) {
            fzgamerules$updateResetButton((AbstractGameRulesScreen.RuleEntry) (Object) this, ruleList);
        }

        return mx >= getX() && mx < this.fzgamerules$resetButton.getRight();
    }

    @Override
    protected boolean modifyIsMouseOver(boolean original, double mx, double my) {
        return super.modifyIsMouseOver(original, mx, my) || fzgamerules$isMouseOverWithSlack(mx, my);
    }
}
