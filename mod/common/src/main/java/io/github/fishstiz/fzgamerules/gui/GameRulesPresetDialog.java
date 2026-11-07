package io.github.fishstiz.fzgamerules.gui;

import io.github.fishstiz.fidgetz.v0.gui.components.*;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZFlexLayout;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZLayout;
import io.github.fishstiz.fidgetz.v0.gui.renderables.RenderableRectangle;
import io.github.fishstiz.fidgetz.v0.gui.renderables.Renderables;
import io.github.fishstiz.fidgetz.v0.gui.state.FZMutableRef;
import io.github.fishstiz.fidgetz.v0.utils.GuiGraphicsUtils;
import io.github.fishstiz.fidgetz.v0.utils.ScreenRectangleUtils;
import io.github.fishstiz.fzgamerules.FZGameRules;
import io.github.fishstiz.fzgamerules.inject.RuleListContext;
import io.github.fishstiz.fzgamerules.inject.mixin.AbstractGameRulesScreenAccess;
import io.github.fishstiz.fzgamerules.presets.GameRulesPreset;
import io.github.fishstiz.fzgamerules.presets.GameRulesPresetManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.AbstractGameRulesScreen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRules;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public final class GameRulesPresetDialog {
    private static final SystemToast.SystemToastId CREATE_FAIL_ID = new SystemToast.SystemToastId();
    private static final Component DEFAULT_PRESET_DESC = Component.translatable("fishstizgamerules.preset").withColor(TextColor.GRAY);
    private static final Component PRESETS_TEXT = Component.translatable("fishstizgamerules.presets").withStyle(ChatFormatting.BOLD);
    private static final Component NEW_PRESET_TEXT = Component.translatable("fishstizgamerules.preset.new").withStyle(ChatFormatting.BOLD);
    private static final Component EDIT_PRESET_TEXT = Component.translatable("fishstizgamerules.preset.edit").withStyle(ChatFormatting.BOLD);
    private static final Component NAME_TEXT = Component.translatable("fishstizgamerules.preset.name").append(Component.literal("*").withColor(TextColor.RED));
    private static final Component DESC_TEXT = Component.translatable("fishstizgamerules.preset.description");
    private static final Component SAVE_RULES_TEXT = Component.translatable("fishstizgamerules.preset.save_game_rules");
    private static final Component SAVE_RULES_INFO = Component.translatable("fishstizgamerules.preset.save_game_rules.info");
    private static final int ICON_SIZE = 32;
    private static final Identifier SELECT_SPRITE = Identifier.withDefaultNamespace("transferable_list/select");
    private static final Identifier SELECT_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("transferable_list/select_highlighted");
    private static final WidgetElements ADD_SPRITE = new WidgetElements(FZGameRules.id("icon/plus"), 16, 16);
    private static final WidgetElements REFRESH_SPRITE = new WidgetElements(FZGameRules.id("icon/arrow_clockwise"), 16, 16);
    private static final WidgetElements FOLDER_SPRITE = new WidgetElements(FZGameRules.id("icon/folder"), 16, 16);
    private static final WidgetElements CLOSE_SPRITE = new WidgetElements(FZGameRules.id("icon/cross"), 16, 16);
    private static final WidgetElements EDIT_SPRITE = new WidgetElements(FZGameRules.id("icon/pen_square"), 16, 16);
    private static final WidgetElements DELETE_SPRITE = new WidgetElements(FZGameRules.id("icon/trash"), 16, 16);
    private static final int WIDTH = 300;
    private static final int NAME_MAX_LENGTH = 32;
    private static final int DESC_MAX_LENGTH = 80;
    private static final int SPACING = 8;

    public static void startDialog(Minecraft minecraft, AbstractGameRulesScreen screen) {
        AbstractGameRulesScreenAccess screenAccess = (AbstractGameRulesScreenAccess) screen;
        startDialog(minecraft.gui, minecraft.getTextureManager(), screen, screenAccess.getGameRules(), preset -> {
            GameRules gameRules = screenAccess.getGameRules();
            preset.applyGameRules(gameRules);
            if (screenAccess.getRuleList() instanceof RuleListContext ctx) {
                ctx.fzgamerules$refreshEntries();
            }
        });
    }

    public static void startDialog(
            Gui gui,
            TextureManager textureManager,
            Screen screen,
            GameRules gameRules,
            Consumer<GameRulesPreset> onSelect
    ) {
        GameRulesPresetManager manager = new GameRulesPresetManager(textureManager);

        FZLayoutList presetList = FZLayoutList.builder()
                .contentPadding(SPACING)
                .maxContentWidth(0)
                .onRefresh(e -> {
                    e.layout().spacing(8).defaultChildSettings().flexCross();

                    e.layout().spacer().height(SPACING / 2);

                    List<GameRulesPreset> presets = manager.getPresets();
                    if (presets.isEmpty()) {
                        e.layout().child(
                                FZText.builder(Component.translatable("fishstizgamerules.presets.empty")).build(),
                                e.layout().newChildSettings().unflex().alignHorizontallyCenter()
                        );
                    }

                    for (GameRulesPreset preset : presets) {
                        FZFlexLayout layout = e.layout().child(FZFlexLayout.horizontal().spacing(SPACING));
                        layout.defaultChildSettings().alignVerticallyMiddle();

                        RenderableRectangle icon = createPresetIcon(preset, manager);
                        layout.child(FZIconButton.builder(icon)
                                .icon(createSelectIcon(layout))
                                .size(ICON_SIZE, ICON_SIZE)
                                .onPress(() -> {
                                    onSelect.accept(preset);
                                    gui.setScreen(screen);
                                })
                                .build());

                        FZFlexLayout details = layout.child(FZFlexLayout.vertical(), layout.flexChildSettings());
                        details.spacing(4).defaultChildSettings().flexCross();

                        details.child(FZText.builder(Component.literal(preset.getName())).build());

                        String desc = preset.getDescription();
                        details.child(
                                new WrappingTextWidget(desc.isEmpty()
                                        ? DEFAULT_PRESET_DESC
                                        : Component.literal(desc).withColor(TextColor.GRAY)
                                ),
                                details.flexChildSettings()
                        );

                        layout.child(FZIconButton.builder()
                                .square()
                                .icon(EDIT_SPRITE)
                                .onPress(() -> gui.setScreen(new ModalScreen(screen, EDIT_PRESET_TEXT, createFormLayout(
                                        gui,
                                        screen,
                                        e.target(),
                                        manager,
                                        gameRules,
                                        preset,
                                        icon
                                ))))
                                .build());
                    }

                    e.layout().spacer().height(SPACING / 2);
                })
                .build();

        gui.setScreen(new ModalScreen(screen, PRESETS_TEXT, createListLayout(gui, screen, manager, gameRules, presetList)));
    }

    private static WidgetElements createSelectIcon(Layout layout) {
        return new WidgetElements(
                (graphics, left, top, width, height, mouseX, mouseY, _) -> {
                    if (ScreenRectangleUtils.containsPoint(layout.getX(), layout.getY(), layout.getWidth(), layout.getHeight(), mouseX, mouseY)) {
                        GuiGraphicsUtils.sprite(graphics, SELECT_SPRITE, left, top, width, height);
                    }
                },
                Renderables.sprite(SELECT_HIGHLIGHTED_SPRITE),
                ICON_SIZE,
                ICON_SIZE
        );
    }

    private static RenderableRectangle createPresetIcon(GameRulesPreset preset, GameRulesPresetManager manager) {
        return (graphics, left, top, width, height, _, _, _) ->
                GuiGraphicsUtils.texture(graphics, manager.getIcon(preset), left, top, width, height);
    }

    private static FZLayout createListLayout(Gui gui, Screen screen, GameRulesPresetManager manager, GameRules gameRules, FZLayoutList presetList) {
        FZFlexLayout layout = FZFlexLayout.vertical().maxWidth(WIDTH).spacing(SPACING);
        FZFlexLayout header = layout.child(FZFlexLayout.horizontal().spacing(SPACING), layout.flexChildHorizontalSettings());

        header.child(FZText.builder(PRESETS_TEXT).build(), header.flexChildSettings());

        header.child(FZIconButton.builder()
                .square()
                .icon(ADD_SPRITE)
                .onPress(() -> gui.setScreen(new ModalScreen(screen, NEW_PRESET_TEXT, createFormLayout(
                        gui,
                        screen,
                        presetList,
                        manager,
                        gameRules,
                        null,
                        null
                ))))
                .build());

        header.child(FZIconButton.builder()
                .square()
                .icon(REFRESH_SPRITE)
                .onPress(() -> {
                    manager.refreshPresets();
                    presetList.refreshEntries();
                })
                .build());

        header.child(FZIconButton.builder()
                .square()
                .icon(FOLDER_SPRITE)
                .onPress(() -> {
                    try {
                        GameRulesPresetManager.createAndOpenDirectory();
                    } catch (IOException e) {
                        FZGameRules.LOG.error("Failed to open preset directory.", e);
                    }
                })
                .build());

        header.child(FZIconButton.builder()
                .square()
                .icon(CLOSE_SPRITE)
                .onPress(() -> gui.setScreen(screen))
                .build());

        layout.child(presetList, layout.flexChildSettings().preferredFlexHeight(300));

        return layout;
    }

    private static FZLayout createFormLayout(
            Gui gui,
            Screen screen,
            FZLayoutList presetList,
            GameRulesPresetManager manager,
            GameRules gameRules,
            @Nullable GameRulesPreset preset,
            @Nullable RenderableRectangle icon
    ) {
        FZFlexLayout layout = FZFlexLayout.vertical().maxWidth(WIDTH).spacing(SPACING);
        layout.defaultChildSettings().flexCross();

        FZFlexLayout header = layout.child(FZFlexLayout.horizontal().spacing(SPACING));

        if (icon != null) {
            header.child(FZIcon.builder(icon).size(20, 20).build());
        }

        header.child(FZText.builder(preset == null ? NEW_PRESET_TEXT : EDIT_PRESET_TEXT).build(), header.flexChildSettings());

        if (preset != null) {
            header.child(FZIconButton.builder().square()
                    .square()
                    .icon(DELETE_SPRITE)
                    .onPress(() -> {
                        manager.deletePreset(preset);
                        presetList.refreshEntries();
                        gui.setScreen(new ModalScreen(screen, PRESETS_TEXT, createListLayout(
                                gui,
                                screen,
                                manager,
                                gameRules,
                                presetList
                        )));
                    })
                    .build());
        }

        header.child(FZIconButton.builder()
                .square()
                .icon(CLOSE_SPRITE)
                .onPress(() -> gui.setScreen(screen))
                .build());

        FZMutableRef<Draft> draft = new FZMutableRef<>(Draft.of(preset));

        Runnable submitHandler = () -> {
            boolean success = true;
            if (preset != null) {
                manager.setName(preset, draft.value().name);
                manager.setDescription(preset, draft.value().description);
                manager.savePreset(preset);
            } else {
                try {
                    manager.savePreset(manager.createPreset(draft.value().name, draft.value().description, gameRules));
                } catch (IOException e) {
                    SystemToast.addOrUpdate(
                            gui.toastManager(),
                            CREATE_FAIL_ID,
                            Component.translatable("fishstizgamerules.preset.new.fail"),
                            null
                    );
                    FZGameRules.LOG.error("Failed to create preset {}", draft.value().name, e);
                    success = false;
                }
            }

            if (success) {
                boolean focused = presetList.isFocused();
                presetList.refreshEntries();
                ModalScreen presetsModal = new ModalScreen(
                        screen,
                        PRESETS_TEXT,
                        createListLayout(gui, screen, manager, gameRules, presetList)
                );
                gui.setScreen(presetsModal);
                if (focused) {
                    presetsModal.setFocused(presetList);
                }
            }
        };

        layout.child(FZTextField.bind("Name", draft.map(Draft::name).map(name -> FZTextField.builder()
                .hint(NAME_TEXT)
                .text(name)
                .maxLength(NAME_MAX_LENGTH)
                .onChange(e -> draft.set(state -> state.withName(e.value())))
                .onConfirm(e -> {
                    if (draft.value().valid()) {
                        submitHandler.run();
                        e.confirm();
                    }
                })
                .toProps())));

        layout.child(FZTextField.bind("Description", draft.map(Draft::description).map(desc -> FZTextField.builder()
                .hint(DESC_TEXT)
                .text(desc)
                .maxLength(DESC_MAX_LENGTH)
                .onChange(e -> draft.set(state -> state.withDescription(e.value())))
                .onConfirm(e -> {
                    if (draft.value().valid()) {
                        submitHandler.run();
                        e.confirm();
                    }
                })
                .toProps())));

        if (preset != null) {
            layout.child(FZButton.builder()
                    .message(SAVE_RULES_TEXT)
                    .tooltip(SAVE_RULES_INFO)
                    .onPress(() -> {
                        manager.setGameRules(preset, gameRules);
                        manager.savePreset(preset);
                    })
                    .build());
        }

        FZFlexLayout footer = layout.child(FZFlexLayout.horizontal().spacing(SPACING));
        footer.defaultChildSettings().flexMain();

        footer.child(FZButton.bind("Save", draft.map(state -> FZButton.builder()
                .active(state.valid())
                .message(CommonComponents.GUI_DONE)
                .onPress(submitHandler)
                .toProps())));

        footer.child(FZButton.builder()
                .message(CommonComponents.GUI_CANCEL)
                .onPress(() -> {
                    boolean focused = presetList.isFocused();
                    ModalScreen presetsModal = new ModalScreen(
                            screen,
                            PRESETS_TEXT,
                            createListLayout(gui, screen, manager, gameRules, presetList)
                    );
                    gui.setScreen(presetsModal);
                    if (focused) {
                        presetsModal.setFocused(presetList);
                    }
                })
                .build());

        return layout;
    }

    private record Draft(String name, String description) {
        Draft withName(String name) {
            return new Draft(name, description);
        }

        Draft withDescription(String description) {
            return new Draft(name, description);
        }

        boolean valid() {
            return !name.isEmpty();
        }

        static Draft of(@Nullable GameRulesPreset preset) {
            if (preset == null) {
                return new Draft("", "");
            } else {
                return new Draft(preset.getName(), preset.getDescription());
            }
        }
    }

    private GameRulesPresetDialog() {
    }
}
