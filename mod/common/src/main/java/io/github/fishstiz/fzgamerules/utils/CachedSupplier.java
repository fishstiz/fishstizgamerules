package io.github.fishstiz.fzgamerules.utils;

import java.util.function.Supplier;

public final class CachedSupplier<T> implements Supplier<T> {
    private final Supplier<T> delegate;
    private T cache;
    private boolean stale = true;

    public CachedSupplier(Supplier<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public T get() {
        if (this.stale) {
            cache = delegate.get();
            this.stale = false;
        }

        return cache;
    }

    public void invalidateCache() {
        this.stale = true;
    }
}
