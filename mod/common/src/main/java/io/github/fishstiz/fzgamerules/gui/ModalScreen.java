package io.github.fishstiz.fzgamerules.gui;

import io.github.fishstiz.fidgetz.v0.gui.layouts.FZComposedLayout;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZLayout;
import io.github.fishstiz.fidgetz.v0.utils.GuiGraphicsUtils;
import io.github.fishstiz.fidgetz.v0.utils.ScreenRectangleUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ModalScreen extends Screen {
    private static final Identifier DEFAULT_BACKGROUND_SPRITE = Identifier.withDefaultNamespace("popup/background");
    private static final int DEFAULT_MARGIN = 8;
    private static final int DEFAULT_PADDING = 12;
    private final Screen lastScreen;
    private final Screen backgroundScreen;
    private final FZLayout layout;
    private ScreenRectangle bounds = ScreenRectangle.empty();

    public ModalScreen(
            Screen lastScreen,
            Screen backgroundScreen,
            Component title,
            FZLayout layout
    ) {
        super(title);
        this.backgroundScreen = backgroundScreen;
        this.lastScreen = lastScreen;
        this.layout = layout;
    }

    public ModalScreen(Screen lastScreen, Component title, FZLayout layout) {
        this(lastScreen, lastScreen, title, layout);
    }

    @Override
    protected void init() {
        layout.visitWidgets(this::addRenderableWidget);
        repositionElements();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        backgroundScreen.extractBackground(graphics, mouseX, mouseY, a);
        graphics.nextStratum();
        backgroundScreen.extractRenderState(graphics, -1, -1, a);
        graphics.nextStratum();
        extractBlurredBackground(graphics);

        GuiGraphicsUtils.sprite(graphics, DEFAULT_BACKGROUND_SPRITE, bounds.left(), bounds.top(), bounds.width(), bounds.height());
    }

    @Override
    protected void repositionElements() {
        backgroundScreen.resize(this.width, this.height);
        this.bounds = ScreenRectangleUtils.shrink(
                FZComposedLayout.contain(this, layout)
                        .padding(DEFAULT_MARGIN + DEFAULT_PADDING)
                        .center()
                        .clamp()
                        .arrange()
                        .get()
                        .getRectangle(),
                ScreenRectangleUtils.insets(DEFAULT_MARGIN)
        );
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(lastScreen);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }

        if (!this.bounds.containsPoint((int) event.x(), (int) event.y())) {
            onClose();
        }

        return true;
    }
}
