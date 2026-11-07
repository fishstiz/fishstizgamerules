package io.github.fishstiz.fzgamerules.presets;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import io.github.fishstiz.fzgamerules.FZGameRules;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;

public class PresetIconCache {
    private static final String EXT = ".png";
    private static final List<Identifier> BUILT_IN = List.of(
            FZGameRules.id("textures/presets/icon_red.png"),
            FZGameRules.id("textures/presets/icon_orange.png"),
            FZGameRules.id("textures/presets/icon_yellow.png"),
            FZGameRules.id("textures/presets/icon_green.png"),
            FZGameRules.id("textures/presets/icon_blue.png"),
            FZGameRules.id("textures/presets/icon_violet.png"),
            FZGameRules.id("textures/presets/icon_pink.png")
    );
    private final Map<String, Identifier> cache = new ConcurrentHashMap<>();
    private final TextureManager textureManager;
    private final Path directory;

    public PresetIconCache(Path directory, TextureManager textureManager) {
        this.textureManager = textureManager;
        this.directory = directory;
    }

    public Identifier get(String id) {
        if (cache.containsKey(id)) {
            return cache.get(id);
        }

        Path path = directory.resolve(id + EXT);
        if (!Files.exists(path)) {
            return getFallback(id);
        }

        try (InputStream stream = Files.newInputStream(path)) {
            NativeImage image = NativeImage.read(stream);

            Identifier icon = FZGameRules.id(
                    "preset/"
                    + Util.sanitizeName(id, Identifier::validPathChar)
                    + "/"
                    + Hashing.sha256().hashUnencodedChars(id)
                    + "/icon"
            );

            textureManager.register(icon, new DynamicTexture(icon::toString, image));
            cache.put(id, icon);
            return icon;
        } catch (IOException e) {
            FZGameRules.LOG.error("Failed to load preset icon for {}", id, e);
        }

        return getFallback(id);
    }

    private Identifier getFallback(String id) {
        CRC32 crc32 = new CRC32();
        crc32.update(String.valueOf(id).getBytes(StandardCharsets.UTF_8));
        Identifier icon = BUILT_IN.get((int) (crc32.getValue() % BUILT_IN.size()));
        cache.put(id, icon);
        return icon;
    }

    public void remove(String id) {
        cache.remove(id);
    }

    public void invalidate() {
        cache.clear();
    }
}
