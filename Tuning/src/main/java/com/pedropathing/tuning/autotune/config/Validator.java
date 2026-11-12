package com.pedropathing.tuning.autotune.config;

import java.util.Objects;

/**
 * A single check on a value, used by {@link Validation} and enforced by
 * {@link Var#set(Object)}.
 *
 * <p>A validator is a predicate: it returns {@code false} for a value it
 * rejects, it never throws for one. The numeric factories return
 * {@code false} on a null value (instead of throwing on unboxing), so a
 * null value fails validation with {@link IllegalArgumentException} from
 * {@link Validation#validate(Object)} rather than a
 * {@link NullPointerException} — use {@link #nonnull()} when a value must
 * not be null.
 */
@FunctionalInterface
public interface Validator<T> {
    boolean validate(T value);

    static Validator<Double> positive() {
        return v -> v != null && v > 0.0;
    }

    static Validator<Double> nonnegative() {
        return v -> v != null && v >= 0.0;
    }

    static Validator<Double> negative() {
        return v -> v != null && v < 0.0;
    }

    static Validator<Double> nonpositive() {
        return v -> v != null && v <= 0.0;
    }

    static <T> Validator<T> nonnull() {
        return Objects::nonNull;
    }

    static Validator<Double> inRange(double min, double max) {
        return v -> v != null && v >= min && v <= max;
    }

    static Validator<Double> nonZero() {
        return v -> v != null && v != 0.0;
    }
}
