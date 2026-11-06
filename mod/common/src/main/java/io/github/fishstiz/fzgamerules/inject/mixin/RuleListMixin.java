package io.github.fishstiz.fzgamerules.inject.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.fishstiz.fzgamerules.inject.RuleEntryContext;
import io.github.fishstiz.fzgamerules.inject.RuleListContext;
import net.minecraft.client.gui.screens.worldselection.AbstractGameRulesScreen;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(AbstractGameRulesScreen.RuleList.class)
abstract class RuleListMixin implements RuleListContext {
    @Unique
    private String fz$gamerules$filter = "";

    @Shadow
    public abstract void populateChildren(String filter);

    @Inject(method = "populateChildren", at = @At("HEAD"))
    private void onPopulateChildren(String filter, CallbackInfo ci) {
        this.fz$gamerules$filter = filter;
    }

    @Override
    public void fzgamerules$refreshEntries() {
        populateChildren(this.fz$gamerules$filter);
    }

    @Inject(method = "lambda$populateChildren$0", at = @At("HEAD"))
    private void onPopulateCategories(
            Map.Entry<GameRuleCategory, Object> e,
            CallbackInfo ci,
            @Share("category") LocalRef<GameRuleCategory> categoryRef
    ) {
        categoryRef.set(e.getKey());
    }

    @ModifyExpressionValue(method = "lambda$populateChildren$0", at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/client/gui/screens/worldselection/AbstractGameRulesScreen;Lnet/minecraft/network/chat/Component;)Lnet/minecraft/client/gui/screens/worldselection/AbstractGameRulesScreen$CategoryRuleEntry;"
    ))
    private AbstractGameRulesScreen.CategoryRuleEntry bindGameRuleCategory(
            AbstractGameRulesScreen.CategoryRuleEntry original,
            @Share("category") LocalRef<GameRuleCategory> categoryRef
    ) {
        ((RuleEntryContext) original).fzgamerules$setGameRuleCategory(categoryRef.get());
        return original;
    }
}
