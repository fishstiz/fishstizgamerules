package io.github.fishstiz.fzgamerules.inject.mixin;

import io.github.fishstiz.fzgamerules.gui.FZGameRulesInWorldScreen;
import io.github.fishstiz.fzgamerules.gui.FZGameRulesCreateWorldScreen;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.InWorldGameRulesScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationGameRulesScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Gui.class)
abstract class GuiMixin {
    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen replaceGameRulesScreen(Screen screen) {
        if (screen instanceof WorldCreationGameRulesScreen gameRulesScreen) {
            return FZGameRulesCreateWorldScreen.fromVanilla(gameRulesScreen);
        } else if (screen instanceof InWorldGameRulesScreen inWorldGameRulesScreen) {
            return FZGameRulesInWorldScreen.fromVanilla(inWorldGameRulesScreen);
        }

        return screen;
    }
}
