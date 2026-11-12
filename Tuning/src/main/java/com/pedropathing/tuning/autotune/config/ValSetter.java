package com.pedropathing.tuning.autotune.config;

import java.util.function.Consumer;

@FunctionalInterface
public interface ValSetter<T> extends Consumer<T> {
    void set(T t);
    @Override
    default void accept(T t) {
        set(t);
    }
}
