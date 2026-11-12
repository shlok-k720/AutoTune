package com.pedropathing.tuning.autotune.config;

/**
 * A group of config variables — the dashboard section. Subclass it, assign
 * the group's {@link Var}s / {@link ConfigVar}s in {@link
 * IConfiguration#defaults()}, and optionally override them with a
 * {@link Configurator} lambda passed to the constructor:
 *
 * <pre>
 * public class ShooterConfig extends Configuration&lt;ShooterConfig&gt; {
 *     public ConfigVar&lt;Double&gt; kP;
 *     public ConfigVar&lt;Double&gt; kF;
 *
 *     public ShooterConfig() {
 *         super(config -&gt; {});
 *     }
 *
 *     public ShooterConfig(Configurator&lt;ShooterConfig&gt; configurator) {
 *         super(configurator);
 *     }
 *
 *     {@literal @}Override
 *     public void defaults() {
 *         kP = ConfigVar.of("Shooter kP", 0.1, Validator.positive());
 *         kF = ConfigVar.of("Shooter kF", 0.0, Validator.nonnegative());
 *     }
 * }
 *
 * ShooterConfig shooter = new ShooterConfig(c -&gt; c.kP.set(0.2));
 * </pre>
 *
 * <p><b>Trap — read this before writing a subclass.</b>
 * {@link IConfiguration#defaults()} runs during the superclass
 * constructor, <i>before</i> the subclass's field initializers run. So a
 * config field must be <i>assigned</i> in {@code defaults()}, never
 * declared with an initializer — a declaration like
 * {@code public ConfigVar<Double> kP = ConfigVar.of(...);} would run
 * after {@code defaults()} and silently replace the var the configurator
 * just tuned.
 *
 * @param <T> the concrete config class
 */
public abstract class Configuration<T extends IConfiguration<T>> implements IConfiguration<T> {
    protected Configurator<T> configurator;
    public Configuration(Configurator<T> configurator) {
        this.configurator = configurator;
        config();
    }

    protected Configuration<T> config() {
        return (Configuration<T>) config(configurator);
    }
}
