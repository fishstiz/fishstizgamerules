package io.github.fishstiz.fzgamerules.inject;

import io.github.fishstiz.fidgetz.v0.gui.components.*;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZComposedLayout;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZFlexLayout;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZLayout;
import io.github.fishstiz.fzgamerules.gui.CategoryList;
import io.github.fishstiz.fzgamerules.inject.mixin.AbstractGameRulesScreenAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.worldselection.AbstractGameRulesScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import net.minecraft.world.level.gamerules.GameRules;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.util.Objects;
import java.util.Optional;

public interface GameRulesScreenContext {
    static int fzgamerules$maxWidth() {
        return 650;
    }

    static int fzgamerules$spacing() {
        return 8;
    }

    static int fzgamerules$resetButtonWidth() {
        return 20;
    }

    default Optional<AbstractGameRulesScreen.RuleList> fzgamerules$createRuleList(GameRules gameRules) {
        return Optional.empty();
    }

    default void fzgamerules$setSearchBox(FZTextField searchBox) {
    }

    default void fzgamerules$setRuleList(AbstractGameRulesScreen.RuleList ruleList) {
    }

    default void fzgamerules$setCategoryList(CategoryList categoryList) {
    }

    default void fzgamerules$setDoneButton(Button doneButton) {
    }

    default void fzgamerules$showPresetDialog() {
    }

    default boolean fzgamerules$shouldIncludeReset() {
        return false;
    }

    default FZLayout fzgamerules$createLayout(AbstractGameRulesScreen screen, GameRules gameRules) {
        FZFlexLayout baseLayout = FZFlexLayout.horizontal(screen).spacing(fzgamerules$spacing());
        {
            FZFlexLayout sideLayout = baseLayout.child(FZFlexLayout.vertical(), baseLayout.flexChildSettings().maxFlexWidth(150));
            sideLayout.spacing(fzgamerules$spacing());

            fzgamerules$setSearchBox(sideLayout.child(
                    FZTextField.builder()
                            .width(0)
                            .hint(AbstractGameRulesScreenAccess.getSEARCH_HINT())
                            .onChange(e -> ((AbstractGameRulesScreenAccess) screen).invokeFilterGameRules(e.value()))
                            .build(),
                    sideLayout.flexChildHorizontalSettings()
            ));

            fzgamerules$setCategoryList(sideLayout.child(new CategoryList(), sideLayout.flexChildSettings()));
        }
        {
            FZFlexLayout mainLayout = baseLayout.child(FZFlexLayout.vertical(), baseLayout.flexChildSettings().minFlexWidth(180));
            mainLayout.spacing(fzgamerules$spacing());

            {
                FZFlexLayout headerLayout = mainLayout.child(FZFlexLayout.horizontal(), mainLayout.flexChildHorizontalSettings());
                headerLayout.spacing(fzgamerules$spacing());

                headerLayout.child(
                        FZText.builder(screen.getTitle().plainCopy().withStyle(ChatFormatting.BOLD))
                                .height(20)
                                .build(),
                        headerLayout.flexChildHorizontalSettings()
                );

                headerLayout.child(FZButton.builder()
                        .smallWidth()
                        .message(Component.translatable("controls.reset"))
                        .onPress(() -> {
                            MutableBoolean shouldRefresh = new MutableBoolean(false);
                            AbstractGameRulesScreenAccess screenAccess = (AbstractGameRulesScreenAccess) screen;

                            screenAccess.getGameRules().visitGameRuleTypes(new GameRuleTypeVisitor() {
                                @Override
                                public <T> void visit(GameRule<T> gameRule) {
                                    GameRules currentGameRules = screenAccess.getGameRules();
                                    T value = currentGameRules.get(gameRule);

                                    if (!Objects.equals(value, gameRule.defaultValue())) {
                                        currentGameRules.set(gameRule, gameRule.defaultValue(), null);
                                        shouldRefresh.setTrue();
                                    }
                                }
                            });

                            if (shouldRefresh.booleanValue() && screenAccess.getRuleList() instanceof RuleListContext ctx) {
                                ctx.fzgamerules$refreshEntries();
                            }
                        })
                        .build());
            }

            fzgamerules$setRuleList(mainLayout.child(
                    fzgamerules$createRuleList(gameRules).orElseThrow(),
                    mainLayout.flexChildSettings()
            ));

            {
                FZFlexLayout footerLayout = mainLayout.child(
                        FZFlexLayout.horizontal(),
                        mainLayout.flexChildHorizontalSettings()
                );
                footerLayout.spacing(fzgamerules$spacing()).defaultChildSettings().flexMain();

                footerLayout.child(FZButton.builder()
                        .message(Component.translatable("fishstizgamerules.presets").append(CommonComponents.ELLIPSIS))
                        .focusOnInteraction(false)
                        .onPress(this::fzgamerules$showPresetDialog)
                        .build());

                fzgamerules$setDoneButton(footerLayout.child(FZButton.builder()
                        .message(CommonComponents.GUI_DONE)
                        .onPress(((AbstractGameRulesScreenAccess) screen)::invokeOnDone)
                        .build()));

                footerLayout.child(FZButton.builder().message(CommonComponents.GUI_CANCEL).onPress(screen::onClose).build());
            }
        }

        return FZComposedLayout.contain(screen, baseLayout).padding(fzgamerules$spacing()).clamp().get();
    }
}
