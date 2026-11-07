package io.github.fishstiz.fzgamerules;

import com.mojang.blaze3d.Blaze3D;
import io.github.fishstiz.fzgamerules.platform.Services;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public final class FZGameRules {
    public static final String MOD_ID = "fishstizgamerules";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);

    private FZGameRules() {
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static Path configDir() {
        return Services.PLATFORM.getConfigDir().resolve(MOD_ID);
    }

    public static void openPath(Path path) {
        Blaze3D.openPath(path);
    }
}
