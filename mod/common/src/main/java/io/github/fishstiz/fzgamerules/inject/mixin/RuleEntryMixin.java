package io.github.fishstiz.fzgamerules.inject.mixin;

import io.github.fishstiz.fidgetz.v0.gui.components.FZContextMenu;
import io.github.fishstiz.fzgamerules.inject.RuleEntryContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.AbstractGameRulesScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.world.level.gamerules.GameRule;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractGameRulesScreen.RuleEntry.class)
abstract class RuleEntryMixin extends AbstractSelectionListEntryMixin implements RuleEntryContext, FZContextMenu.Source {
    @Unique
    private GameRule<?> fzgamerules$gameRule;

    @Override
    public <T> void fzgamerules$setGameRule(GameRule<T> gameRule) {
        this.fzgamerules$gameRule = gameRule;
    }

    @Override
    public @Nullable GameRule<?> fzgamerules$getGameRule() {
        return this.fzgamerules$gameRule;
    }

    @Override
    public void fidgetz$updateContextEntries(double x, double y, FZContextMenu.Collector collector) {
        if (fzgamerules$gameRule != null) {
            collector.addEntry(builder -> builder
                    .onPress(() -> Minecraft.getInstance().keyboardHandler.setClipboard(fzgamerules$gameRule.id()))
                    .message(CommonComponents.GUI_COPY_TO_CLIPBOARD));
        }
    }
}
