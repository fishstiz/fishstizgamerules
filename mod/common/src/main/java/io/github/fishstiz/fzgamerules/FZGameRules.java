package io.github.fishstiz.fzgamerules;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FZGameRules {
    public static final String MOD_ID = "fishstizgamerules";
    public static final String MOD_NAME = "Fishstiz Game Rules Screen";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);

    private FZGameRules() {
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
