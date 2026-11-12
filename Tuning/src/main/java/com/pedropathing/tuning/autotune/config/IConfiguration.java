package com.pedropathing.tuning.autotune.config;

@FunctionalInterface
public interface IConfiguration<T extends IConfiguration<T>> {
    void defaults();

    default IConfiguration<T> config(Configurator<T> configurator) {
        defaults();
        return configurator.configureAndReturn((T) this);
    }
}
