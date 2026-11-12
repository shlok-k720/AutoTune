package com.pedropathing.tuning.autotune.config.kt

import com.pedropathing.tuning.autotune.config.Var

/**
 * Typed aliases and named factories for [Var] (no `PosVar` — this library
 * has no `Pose2d`).
 */
public typealias DoubleVar = Var<Double>
public typealias IntVar = Var<Int>
public typealias StrVar = Var<String>
public typealias BoolVar = Var<Boolean>

public inline fun DoubleVar(name: String, value: Double): DoubleVar = Var<Double>(name, value)

public inline fun IntVar(name: String, value: Int): IntVar = Var<Int>(name, value)

public inline fun StrVar(name: String, value: String): StrVar = Var<String>(name, value)

public inline fun BoolVar(name: String, value: Boolean): BoolVar = Var<Boolean>(name, value)
