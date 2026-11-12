package com.pedropathing.tuning.autotune.config;

/**
 * A read-only variable: a value read through a {@link ValGetter}.
 *
 * <p>A {@code Val} is either a constant ({@link #Val(Object)}) or a live
 * view of some other source ({@link #Val(ValGetter)}), for example a sensor
 * reading or a value computed from other config. It never changes the
 * source — for a read/write variable, see {@link Var}.
 *
 * <pre>
 * Val&lt;Double&gt; maxSpeed = new Val&lt;&gt;(0.8);
 * Val&lt;Double&gt; liveSpeed = new Val&lt;&gt;(drive::getSpeed);
 * double speed = liveSpeed.get();
 * </pre>
 *
 * @param <T> the value type
 */
public class Val<T> implements ValGetter<T> {
    private final ValGetter<T> getter;

    /** A constant value: {@link #get()} always returns {@code value}. */
    public Val(T value) {
        this(() -> value);
    }

    /**
     * A live view: {@link #get()} returns whatever {@code getter} returns
     * at the time it is called.
     */
    public Val(ValGetter<T> getter) {
        this.getter = getter;
    }

    @Override
    public T get() {
        return getter.get();
    }

    @Override
    public String toString() {
        return "Val(" + get() + ")";
    }
}
