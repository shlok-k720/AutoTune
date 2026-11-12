/**
 * Config variables for AutoTune procedures: named, validated, lockable
 * values with a global registry, plus Pedro-style factories and
 * lambda-grouped config classes.
 *
 * <p>What lives here:
 * <ul>
 *   <li>{@link com.pedropathing.tuning.autotune.config.Var} /
 *       {@link com.pedropathing.tuning.autotune.config.Val} — read/write
 *       and read-only variables ({@link
 *       com.pedropathing.tuning.autotune.config.VarProvider} backs both).
 *   <li>{@link com.pedropathing.tuning.autotune.config.ConfigVar} —
 *       the form procedure authors should usually use:
 *       {@code of(name, value, validators...)} (validated initial value),
 *       {@code ofNullable(...)} (null allowed),
 *       {@code required(name, validators...)} (no value until set).
 *   <li>{@link com.pedropathing.tuning.autotune.config.Validator} /
 *       {@link com.pedropathing.tuning.autotune.config.Validation} /
 *       {@link com.pedropathing.tuning.autotune.config.Modifier} /
 *       {@link com.pedropathing.tuning.autotune.config.Section} /
 *       {@link com.pedropathing.tuning.autotune.config.Variables} /
 *       {@link com.pedropathing.tuning.autotune.config.GarbageCollector}
 *       — validation, temporary overrides, dashboard grouping, the global
 *       registry, and disposal.
 *   <li>{@link com.pedropathing.tuning.autotune.config.Configuration} /
 *       {@link com.pedropathing.tuning.autotune.config.Configurator} /
 *       {@link com.pedropathing.tuning.autotune.config.IConfiguration} —
 *       group a procedure's constants into a config class (the dashboard
 *       section); a {@code Configurator} lambda overrides defaults.
 * </ul>
 *
 * <p>Rules: only named vars self-register in
 * {@link com.pedropathing.tuning.autotune.config.Variables}; {@code set()}
 * enforces the mutability lock first, then validation; a plain
 * {@code Var} constructor does <i>not</i> validate its initial value
 * (use {@code ConfigVar.of} when it must be validated); a var with a
 * non-null validator refuses collection by the garbage collector.
 * {@code Var.onSet} / {@code onGet} listeners fire on every successful
 * set / every get, on the calling thread.
 *
 * <p><b>Status: interface complete, integration pending.</b> This package
 * is pure JVM (no FTC SDK / Android dependencies) and compiles standalone.
 * What is <i>not</i> built yet: the transport (a WebSocket message type or
 * HTTP endpoint bridging the {@code Variables} registry to the web-tuner)
 * and the dashboard editor UI (tab 2 global section, tab 1 per-procedure
 * section). The registry's public {@code variables} / {@code modifiers}
 * sets are the enumeration source that integration reads from. See
 * AGENT_CONTEXT.md §4 for the design.
 */
package com.pedropathing.tuning.autotune.config;
