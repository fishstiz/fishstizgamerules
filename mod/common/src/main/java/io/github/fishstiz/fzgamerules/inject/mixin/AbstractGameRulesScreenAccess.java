package io.github.fishstiz.fzgamerules.inject.mixin;

import net.minecraft.client.gui.screens.worldselection.AbstractGameRulesScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.gamerules.GameRules;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(AbstractGameRulesScreen.class)
public interface AbstractGameRulesScreenAccess {
    @Accessor
    static Component getSEARCH_HINT() {
        throw new AssertionError();
    }

    @Accessor
    Consumer<Optional<GameRules>> getExitCallback();

    @Accessor
    GameRules getGameRules();

    @Accessor
    AbstractGameRulesScreen.@Nullable RuleList getRuleList();

    @Invoker
    void invokeFilterGameRules(String filter);

    @Invoker
    void invokeOnDone();
}
