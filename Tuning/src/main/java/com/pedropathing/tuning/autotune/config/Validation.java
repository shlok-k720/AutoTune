package com.pedropathing.tuning.autotune.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A set of {@link Validator}s checked together. {@link #validate(Object)}
 * throws {@link IllegalArgumentException} listing every validator that
 * failed.
 *
 * <p>Validators run in the order they were given (insertion order), so a
 * var that must reject null with {@link IllegalArgumentException} — rather
 * than a {@link NullPointerException} from a numeric validator unboxing
 * it — should list {@link Validator#nonnull()} first. {@link
 * ConfigVar#of(String, Object, Validator[])} does this for you.
 */
public class Validation<T> {
    public Set<Validator<T>> validators;

    public Validation(Collection<Validator<T>> validators) {
        this.validators = Collections.unmodifiableSet(new LinkedHashSet<>(validators));
    }

    public void validate(T value) {
        Set<Validator<T>> failed = new HashSet<>();
        for (Validator<T> validator : validators) {
            if (!validator.validate(value)) {
                failed.add(validator);
            }
        }
        if (!failed.isEmpty()) {
            throw new IllegalArgumentException("Validation failed: " + failed);
        }
    }
}
