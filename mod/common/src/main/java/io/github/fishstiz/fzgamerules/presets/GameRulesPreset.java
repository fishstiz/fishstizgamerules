package io.github.fishstiz.fzgamerules.presets;

import io.github.fishstiz.fzgamerules.FZGameRules;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;

import java.util.Map;
import java.util.stream.Collectors;

public class GameRulesPreset {
    private final transient String id;
    private String name = "";
    private String description = "";
    private Map<String, String> gameRules = Map.of();

    @SuppressWarnings("unused")
    private GameRulesPreset() {
        this.id = "";
    }

    GameRulesPreset(String id) {
        this.id = id;
    }

    void setGameRules(GameRules gameRules) {
        this.gameRules = gameRules.availableRules().collect(Collectors.toMap(GameRule::id, gameRules::getAsString));
    }

    void setName(String name) {
        this.name = name;
    }

    void setDescription(String description) {
        this.description = description;
    }

    GameRulesPreset createCopy(String id) {
        GameRulesPreset preset = new GameRulesPreset(id);
        preset.name = name;
        preset.description = description;
        preset.gameRules = gameRules;
        return preset;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public void applyGameRules(GameRules gameRules) {
        for (Map.Entry<String, String> entry : this.gameRules.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            try {
                BuiltInRegistries.GAME_RULE.get(Identifier.parse(key))
                        .map(Holder.Reference::value)
                        .ifPresent(gameRule -> gameRule.deserialize(value).ifSuccess(v -> setGameRule(gameRules, gameRule, v)));
            } catch (Exception e) {
                FZGameRules.LOG.error("Failed to apply game rule {} from preset {}. ", key + "=" + value, name, e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void setGameRule(GameRules gameRules, GameRule<?> gameRule, T value) {
        gameRules.set(((GameRule<T>) gameRule), value, null);
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof GameRulesPreset preset)) return false;
        return id.equals(preset.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
