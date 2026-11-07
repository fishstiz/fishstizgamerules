package io.github.fishstiz.fzgamerules.presets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.fishstiz.fzgamerules.FZGameRules;
import io.github.fishstiz.fzgamerules.utils.CachedSupplier;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FileUtil;
import net.minecraft.world.level.gamerules.GameRules;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.Pattern;

public final class GameRulesPresetManager {
    public static final Path DIRECTORY = FZGameRules.configDir().resolve("presets");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String EXT = ".json";
    private final Set<GameRulesPreset> dirty = new HashSet<>();
    private final CachedSupplier<Map<String, GameRulesPreset>> presets = new CachedSupplier<>(() -> {
        Map<String, GameRulesPreset> presets = new LinkedHashMap<>();
        if (!Files.isDirectory(DIRECTORY)) return presets;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(DIRECTORY)) {
            for (Path path : stream) {
                try {
                    String fileName = path.getFileName().toString();
                    if (fileName.endsWith(EXT)) {
                        String id = removeExt(fileName);
                        GameRulesPreset preset = GSON.fromJson(Files.readString(path), GameRulesPreset.class);
                        presets.put(id, preset.createCopy(id));
                    }
                } catch (Exception e) {
                    FZGameRules.LOG.error("Failed to read game rules preset '{}'", path.getFileName(), e);
                }
            }
        } catch (IOException e) {
            FZGameRules.LOG.error("Failed to read game rules presets", e);
        }

        return presets;
    });
    private final PresetIconCache iconCache;

    public GameRulesPresetManager(TextureManager textureManager) {
        iconCache = new PresetIconCache(DIRECTORY, textureManager);
    }

    public void refreshPresets() {
        presets.invalidateCache();
        iconCache.invalidate();
    }

    public Optional<GameRulesPreset> getPreset(String id) {
        return Optional.ofNullable(presets.get().get(id));
    }

    public List<GameRulesPreset> getPresets() {
        return presets.get().values().stream().toList();
    }

    public Identifier getIcon(GameRulesPreset preset) {
        return iconCache.get(preset.getId());
    }

    public GameRulesPreset createPreset(String name, String description, GameRules gameRules) throws IOException {
        dirty.forEach(this::savePreset);
        String id = Files.isDirectory(DIRECTORY)
                ? removeExt(FileUtil.findAvailableName(DIRECTORY, name, EXT))
                : FileUtil.sanitizeName(name);

        GameRulesPreset preset = new GameRulesPreset(id);
        preset.setName(name);
        preset.setDescription(description);
        preset.setGameRules(gameRules);
        presets.get().put(id, preset);
        dirty.add(preset);
        return preset;
    }

    public void deletePreset(GameRulesPreset preset) {
        String id = preset.getId();
        presets.get().remove(id);
        iconCache.remove(id);
        dirty.remove(preset);

        try {
            Files.deleteIfExists(DIRECTORY.resolve(id + EXT));
        } catch (IOException e) {
            FZGameRules.LOG.error("Failed to delete game rules preset '{}'", id, e);
        }
    }

    public void setName(GameRulesPreset preset, String name) {
        if (!preset.getName().equals(name)) {
            preset.setName(name);
            dirty.add(preset);
        }
    }

    public void setDescription(GameRulesPreset preset, String description) {
        if (!preset.getDescription().equals(description)) {
            preset.setDescription(description);
            dirty.add(preset);
        }
    }

    public void setGameRules(GameRulesPreset preset, GameRules gameRules) {
        preset.setGameRules(gameRules);
        dirty.add(preset);
    }

    public void savePreset(GameRulesPreset preset) {
        if (dirty.remove(preset) && !preset.getId().isEmpty()) {
            try {
                Files.createDirectories(DIRECTORY);
                String serialized = GSON.toJson(preset, GameRulesPreset.class);
                Path path = DIRECTORY.resolve(preset.getId() + EXT);
                Files.writeString(path, serialized, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                FZGameRules.LOG.error("Failed to save game rules preset '{}'", preset.getId(), e);
            }
        }
    }

    public static void createAndOpenDirectory() throws IOException {
        Files.createDirectories(DIRECTORY);
        FZGameRules.openPath(DIRECTORY);
    }

    private static String removeExt(String path) {
        return path.replaceFirst(Pattern.quote(EXT) + "$", "");
    }
}
