# Pedro Pathing AutoTune — fork

> **This is a fork of [Pedro-Pathing/AutoTune](https://github.com/Pedro-Pathing/AutoTune),
> for adding a tabbed web-tuner, a config-variable interface, telemetry
> injection, and a camera display mode — plus JitPack publishing.
> Upstream is the stock auto-tuner; this fork builds on it.**

Auto tuner for Pedro Pathing. See `.github/AGENT_CONTEXT.md` for the full
brief (what is being built, the repo map, and the hard constraints) and
`.github/DASHBOARD_FEATURES.md` for the FTC Dashboard feature plan.

## What this fork adds (in short)

- **Tabbed web-tuner** — Procedures, OpMode control (with telemetry),
  and Hardware config tabs, implementing FTC Dashboard features in tabs.
  `OpModeProcedure` (an empty scaffold) was deleted; OpMode control is
  built on the OpMode manager instead.
- **Config-variable interface** —
  `Tuning/src/main/java/com/pedropathing/tuning/autotune/config/`:
  named, validated, lockable variables with modifiers and a global
  registry (modelled on FTCUtils Variables), plus Pedro-style
  `ConfigVar.of/required/ofNullable` factories. Interface complete;
  transport to the web UI (WebSocket/HTTP) and the dashboard editor are
  still to be integrated.
- **Telemetry interface** — `TelemetryFinder` wraps the active OpMode's
  telemetry, tracks its items, and is injected in
  `TuningOpMode.runOpMode()`. Interface complete; the WebSocket feed and
  the web-tuner panel are still to be integrated.
- **Camera display mode** — planned `Display` subtype for Limelight /
  standard webcam alongside `Display.image` and `fourWheelBot` (see
  AGENT_CONTEXT.md §6).
- **JitPack publishing** — `maven-publish` with group
  `com.github.shlok-k720.AutoTune`, artifact `tuning`, version 1.0.4;
  `jitpack.yml` pins JDK 17 and builds inside `Tuning/` (the Gradle
  project lives there, not at the repo root). Coordinate:
  `com.github.shlok-k720.AutoTune:tuning:1.0.4` (no JitPack build yet —
  see AGENT_CONTEXT.md §10).
