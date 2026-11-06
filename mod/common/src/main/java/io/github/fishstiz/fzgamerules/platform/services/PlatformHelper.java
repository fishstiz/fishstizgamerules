package io.github.fishstiz.fzgamerules.platform.services;

import java.nio.file.Path;

public interface PlatformHelper {
    String getPlatform();

    Path getConfigDir();

    boolean isModLoaded(String id);

    boolean isDev();
}
