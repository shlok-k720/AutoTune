package com.pedropathing.tuning.autotune.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The global registry of variables: every named {@link Var} registers
 * itself here on construction, and {@link Var#at(Object)} registers its
 * {@link Modifier}. Unnamed vars are not registered.
 *
 * <p>The sets are synchronized: variables are usually created on one
 * thread and read from another (an OpMode, a dashboard callback).
 *
 * <p>This registry is also the enumeration source the dashboard
 * integration reads from — that integration (transport + editor UI) is
 * not built yet; see {@code package-info}.
 */
public class Variables {
    public static GarbageCollector garbageCollector = new GarbageCollector();
    public static Set<Var<?>> variables =
            Collections.synchronizedSet(new HashSet<Var<?>>());
    public static Set<Modifier<?>> modifiers =
            Collections.synchronizedSet(new HashSet<Modifier<?>>());

    /** Registers a variable (called by every named {@link Var} constructor). */
    public static void add(Var<?> var) {
        variables.add(var);
    }

    /** Registers a modifier (called by {@link Var#at(Object)}). */
    public static void addModifier(Modifier<?> modifier) {
        modifiers.add(modifier);
    }

    /**
     * Disposes of a variable: clears its modifiers, nulls and locks it,
     * and removes it from the registry.
     */
    public static void finalizeVariable(Var<?> var) {
        garbageCollector.clearModifiers(modifiers, var);
        garbageCollector.collect(var);
        variables.remove(var);
    }
}
