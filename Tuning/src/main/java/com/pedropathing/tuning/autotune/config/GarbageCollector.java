package com.pedropathing.tuning.autotune.config;

import java.util.Set;

/**
 * Disposal for {@link Variables}: nulls and locks a variable, and drops
 * its modifiers.
 *
 * <p>{@link #clearModifiers(Set, Var)} uses {@code removeIf} (removing
 * from a set while streaming over it throws
 * {@code ConcurrentModificationException}), and {@link #collect(Var)}
 * unlocks the var first so collecting an already-final var works.
 *
 * <p>Note: {@link #collect(Var)} still sets the value to null, so a var
 * with a non-null validator refuses collection (that is the validator
 * doing its job) — {@link Variables#finalizeVariable(Var)} then throws
 * {@link IllegalArgumentException} and the var stays registered.
 */
public class GarbageCollector {
    public GarbageCollector() {}

    public void collect(Var<?> var) {
        var.makeMutable();
        var.set(null);
        var.makeFinal();
    }

    public void clearModifiers(Set<Modifier<?>> modifiers, Var<?> var) {
        modifiers.removeIf(modifier -> modifier.getVar() == var);
    }
}
