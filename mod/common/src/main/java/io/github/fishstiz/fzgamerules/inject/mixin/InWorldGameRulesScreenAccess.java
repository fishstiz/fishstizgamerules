package io.github.fishstiz.fzgamerules.inject.mixin;

import net.minecraft.client.gui.components.LoadingDotsWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.InWorldGameRulesScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(InWorldGameRulesScreen.class)
public interface InWorldGameRulesScreenAccess extends AbstractGameRulesScreenAccess {
    @Accessor
    ClientPacketListener getConnection();

    @Accessor
    Screen getLastScreen();

    @Accessor
    @Nullable LoadingDotsWidget getLoadingDotsWidget();
}
