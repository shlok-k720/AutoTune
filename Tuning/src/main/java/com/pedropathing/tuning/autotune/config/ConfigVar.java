package com.pedropathing.tuning.autotune.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A named config variable with Pedro-style factories — the form a
 * procedure author should usually reach for instead of the plain
 * {@link Var} constructors.
 *
 * <p>The factories close the one gap in {@link Var}: a plain {@code Var}
 * constructor never validates its initial value, so a bad default can
 * sit in the registry until the first {@link #set(Object)}. {@link #of}
 * validates the initial value up front (and rejects null); {@link
 * #ofNullable} validates it but allows null; {@link #required} has no
 * value at all until {@link #set(Object)} is called.
 *
 * <p>A {@code ConfigVar} is a {@link Var} — same registry (named vars
 * self-register in {@link Variables}), same lock-then-validate
 * {@link #set(Object)}, same {@link #at(Object)} modifiers, same
 * {@link #onSet(Runnable)} / {@link #onGet(Runnable)} listeners.
 *
 * <pre>
 * ConfigVar&lt;Double&gt; kP = ConfigVar.of("Shooter kP", 0.1, Validator.positive());
 * ConfigVar&lt;Double&gt; feedforward = ConfigVar.required("Shooter FF", Validator.nonnegative());
 * ConfigVar&lt;String&gt; note = ConfigVar.ofNullable("Note", null);
 * </pre>
 *
 * @param <T> the value type
 */
public class ConfigVar<T> extends Var<T> {
    private final boolean required;
    private boolean hasBeenSet;

    private ConfigVar(String name, T value, VarProvider<T> provider,
                      List<Validator<T>> validators, boolean required) {
        super(name, value, provider, validators);
        this.required = required;
        this.hasBeenSet = false;
    }

    /**
     * A named variable with a validated initial value. The value is
     * checked against an implicit {@link Validator#nonnull()} (checked
     * first, so a null default throws {@link IllegalArgumentException},
     * not a {@link NullPointerException} from a numeric validator) plus
     * {@code validators}, before anything is constructed or registered, so
     * a bad default throws {@link IllegalArgumentException} here instead
     * of sitting in the registry. Later {@link #set(Object)} calls enforce
     * the same set (including non-null).
     */
    @SafeVarargs
    public static <T> ConfigVar<T> of(String name, T value, Validator<T>... validators) {
        List<Validator<T>> all = new ArrayList<>(Arrays.asList(validators));
        all.add(0, Validator.nonnull());
        new Validation<>(all).validate(value);
        return new ConfigVar<>(name, value, null, all, false);
    }

    /**
     * A named, provider-backed variable with a validated initial value:
     * like {@link #of(String, Object, Validator[])} but reads and writes
     * through {@code provider}. The provider's current value is validated
     * up front.
     *
     * <p>Note: a literal {@code null} provider is ambiguous to the
     * compiler (the value and provider overloads both apply) — cast it,
     * or use {@link #ofNullable(String, Object, Validator[])}.
     */
    @SafeVarargs
    public static <T> ConfigVar<T> of(String name, VarProvider<T> provider, Validator<T>... validators) {
        List<Validator<T>> all = new ArrayList<>(Arrays.asList(validators));
        all.add(0, Validator.nonnull());
        new Validation<>(all).validate(provider.get());
        return new ConfigVar<>(name, null, provider, all, false);
    }

    /**
     * A named variable with a validated initial value that may be null.
     * The value is checked against {@code validators} only — no implicit
     * non-null check. Note the numeric {@link Validator} factories return
     * {@code false} on null, so combining a null initial value with one
     * of them still throws {@link IllegalArgumentException}.
     */
    @SafeVarargs
    public static <T> ConfigVar<T> ofNullable(String name, T value, Validator<T>... validators) {
        new Validation<>(Arrays.asList(validators)).validate(value);
        return new ConfigVar<>(name, value, null, Arrays.asList(validators), false);
    }

    /**
     * A named variable with no value yet: {@link #get()} throws
     * {@link IllegalStateException} until {@link #set(Object)} has been
     * called once (and passed validation). Use it for a constant the
     * procedure must be given before it can run.
     */
    @SafeVarargs
    public static <T> ConfigVar<T> required(String name, Validator<T>... validators) {
        return new ConfigVar<>(name, null, null, Arrays.asList(validators), true);
    }

    @Override
    public T get() {
        if (required && !hasBeenSet) {
            throw new IllegalStateException(
                    "ConfigVar \"" + getName() + "\" has not been set yet");
        }
        return super.get();
    }

    @Override
    public void set(T value) {
        super.set(value);
        hasBeenSet = true;
    }

    @Override
    public String toString() {
        if (required && !hasBeenSet) {
            return "ConfigVar(" + getName() + ", <unset>)";
        }
        return "ConfigVar(" + getName() + ", " + super.get() + ")";
    }
}
