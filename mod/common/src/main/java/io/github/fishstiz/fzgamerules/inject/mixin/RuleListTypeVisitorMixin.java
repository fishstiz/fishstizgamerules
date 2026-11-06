package io.github.fishstiz.fzgamerules.inject.mixin;

import io.github.fishstiz.fzgamerules.inject.RuleEntryContext;
import net.minecraft.client.gui.screens.worldselection.AbstractGameRulesScreen;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(targets = "net.minecraft.client.gui.screens.worldselection.AbstractGameRulesScreen$RuleList$1")
abstract class RuleListTypeVisitorMixin {
    @Shadow
    @Final
    private Map<GameRuleCategory, Map<GameRule<?>, AbstractGameRulesScreen.RuleEntry>> val$entries;

    @Inject(method = "addEntry", at = @At("RETURN"))
    private void onAddEntry(GameRule<Object> gameRule, AbstractGameRulesScreen.EntryFactory<Object> entryFactory, CallbackInfo ci) {
        var category = val$entries.get(gameRule.category());
        if (category != null) {
            AbstractGameRulesScreen.RuleEntry entry = category.get(gameRule);
            if (entry instanceof RuleEntryContext ctx) {
                ctx.fzgamerules$setGameRule(gameRule);
            }
        }
    }
}
