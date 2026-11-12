package com.pedropathing.tuning.autotune.config;

import java.util.function.Supplier;

@FunctionalInterface
public interface ValGetter<T> extends Supplier<T> {
}
