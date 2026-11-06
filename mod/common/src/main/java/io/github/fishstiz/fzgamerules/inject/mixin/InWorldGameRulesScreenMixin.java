package io.github.fishstiz.fzgamerules.inject.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.fishstiz.fzgamerules.inject.GameRulesScreenContext;
import net.minecraft.client.gui.screens.options.InWorldGameRulesScreen;
import net.minecraft.client.gui.screens.worldselection.AbstractGameRulesScreen;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InWorldGameRulesScreen.class)
abstract class InWorldGameRulesScreenMixin implements GameRulesScreenContext {
    @WrapOperation(method = "onGameRuleValuesUpdated", at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/client/gui/screens/worldselection/AbstractGameRulesScreen;Lnet/minecraft/world/level/gamerules/GameRules;)Lnet/minecraft/client/gui/screens/worldselection/AbstractGameRulesScreen$RuleList;"
    ))
    private AbstractGameRulesScreen.RuleList replaceRuleList(AbstractGameRulesScreen this$0, GameRules gameRules, Operation<AbstractGameRulesScreen.RuleList> original) {
        return fzgamerules$createRuleList(gameRules).orElseGet(() -> original.call(this$0, gameRules));
    }

    @ModifyExpressionValue(method = "onGameRuleValuesUpdated", at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/client/gui/screens/worldselection/AbstractGameRulesScreen;Lnet/minecraft/world/level/gamerules/GameRules;)Lnet/minecraft/client/gui/screens/worldselection/AbstractGameRulesScreen$RuleList;"
    ))
    private AbstractGameRulesScreen.RuleList onChangeRuleList(AbstractGameRulesScreen.RuleList original) {
        fzgamerules$setRuleList(original);
        return original;
    }
}
