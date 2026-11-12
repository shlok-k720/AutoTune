package com.pedropathing.tuning.autotune.config;

@FunctionalInterface
public interface Configurator<T> {
    void configure(T t);
    default T configureAndReturn(T t) {
        configure(t);
        return t;
    }

}
