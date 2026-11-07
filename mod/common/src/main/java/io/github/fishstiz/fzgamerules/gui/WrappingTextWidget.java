package io.github.fishstiz.fzgamerules.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.AbstractStringWidget;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.Component;

import java.util.Objects;

public class WrappingTextWidget extends AbstractStringWidget {
    private MultiLineLabel multilineLabel;

    public WrappingTextWidget(Font font, Component message) {
        super(0, 0, 0, 0, message, font);
        this.active = false;
        refreshMultilineLabel();
    }

    public WrappingTextWidget(Component message) {
        this(Minecraft.getInstance().font, message);
    }

    private void refreshMultilineLabel() {
        Font font = getFont();
        int maxLines = getHeight() / font.lineHeight;
        this.multilineLabel = MultiLineLabel.create(font, getWidth(), maxLines, getMessage());
    }

    @Override
    public void setMessage(Component message) {
        Component previousMessage = getMessage();
        this.message = message;
        if (!Objects.equals(previousMessage, message)) {
            refreshMultilineLabel();
        }
    }

    @Override
    public void setWidth(int width) {
        int previousWidth = getWidth();
        super.setWidth(width);
        if (previousWidth != width) {
            refreshMultilineLabel();
        }
    }

    @Override
    public void setSize(int width, int height) {
        int previousWidth = getWidth();
        super.setSize(width, height);
        if (previousWidth != width) {
            refreshMultilineLabel();
        }
    }

    @Override
    public void visitLines(ActiveTextCollector output) {
        multilineLabel.visitLines(TextAlignment.LEFT, getX(), getY(), getFont().lineHeight, output);
    }
}
