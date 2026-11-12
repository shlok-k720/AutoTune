# Agent context: AutoTune web-tuner — tabs, config variables, telemetry, camera

Give this whole file to the coding agent as its briefing. It is grounded in the
actual code (verified 2026-11-11), not in the task description's assumptions.
Where the task description is wrong, this file says so — follow this file.

**Version 3 — regenerated 2026-11-11, applied to fork main.** Version 2 was
the survey-only brief. This version records what was actually built on
`main` (see "Status — applied to main" below). The JitPack conversion
(item 6) was finished in files later the same day (see "Status" §6) but
is not yet pushed and has no JitPack build behind it.

## Status — applied to main (2026-11-11)

Worked on `main` @ `6a0b1c2` and pushed as one commit (plus a second
commit deleting `OpModeProcedure`, which the batch-push tool cannot do).
Verified after the push on a fresh clone; Gradle cannot run on this VM,
so the Java/Kotlin changes were compile-checked by hand (see per-item
notes) and the Mac Gradle build is still the real check.

1. **Context in `.github` — DONE.** `.github/AGENT_CONTEXT.md` (this
   file) and `.github/DASHBOARD_FEATURES.md` (the Dashboard feature plan)
   are committed on main.
2. **`OpModeProcedure` deleted — DONE.** `procedures/OpModeProcedure.java`
   and the then-empty `procedures/` directory are gone. Verified: zero
   references anywhere (exhaustive grep, 3 hits all inside its own file
   before deletion). Nothing else changed for this deletion.
3. **Config Variable interface — DONE (interface; integration pending).**
   New package `com.pedropathing.tuning.autotune.config`:
   `Var`, `Val`, `ValGetter`, `ValSetter`, `VarProvider`, `Validation`,
   `Validator` (all 7 factories, null-safe), `Modifier`, `Variables`
   (registry, synchronized sets, garbageCollector initialized),
   `Section`, `GarbageCollector`, `ConfigVar` (Pedro `of` /
   `ofNullable` / `required` factories), `Configuration` /
   `Configurator` / `IConfiguration` (lambda grouping), `kt/Types.kt`
   (DoubleVar/IntVar/StrVar/BoolVar), `package-info.java`.
   Two deviations from FTCUtils, both documented in KDoc: numeric
   `Validator` factories are null-safe (a validator is a pure predicate
   returning false, never throwing NPE); `Validation` uses
   `LinkedHashSet` (insertion order) and `ConfigVar.of()` puts the
   implicit `nonnull()` first. Verified: `javac --release 8` exit 0 for
   all 16 Java files, kotlinc 2.1.20 exit 0 for `Types.kt`, plus a
   throwaway 41-assertion check (registration, validation, locking,
   Modifier, `of`/`required`/`ofNullable`, hooks, finalize, Types) —
   41/41 pass. **Still to integrate:** transport (WebSocket message
   type or HTTP endpoint bridging the `Variables` registry) and the
   dashboard editor UI (tab 2 global, tab 1 per-procedure). No
   persistence anywhere — values are session-only.
4. **FTC Dashboard features analyzed — DONE.**
   `.github/DASHBOARD_FEATURES.md` lists every feature with its tab and
   per-feature implementation steps, in build order (telemetry → config
   vars → OpMode controls → camera → field view → Diagnostics → graphs →
   gamepad last/never). §8 below is the same map in table form.
5. **Telemetry interface — DONE (interface; integration pending).**
   `TelemetryFinder` completed: `telemetryData()` returns a snapshot
   array of tracked items (no longer `null`); `getItems()` /
   `telemetryLines()` / `getLines()` accessors; lines tracked
   (`addLine` records them; `removeLine` removes them); `removeItem`
   keeps the tracked list consistent; `clear()` drops only
   non-retained entries (mirroring `TelemetryImpl.clear()`),
   `clearAll()` drops everything, `update()` mirrors auto-clear;
   `findTelemetry()` is null-safe (returns `null`, never throws);
   `inject()` is total and idempotent (no-ops when nothing can be
   injected, adopts an already-installed wrapper); `getInstance()` is
   thread-safe (initialization-on-demand holder); tracked lists are
   `CopyOnWriteArrayList`s, `telemetry` field is `volatile`;
   `restore()` puts the original telemetry back (never clobbers
   something else) and `isInjected()` queries state. `inject()` is
   wired into `TuningOpMode.runOpMode()` (final choke point) before
   `runTuningOpMode()`, `restore()` in the `finally`; both wrapped in
   `try/catch (RuntimeException)` so tracking can never take down a
   tuning procedure. Known limits (documented in the file's class KDoc):
   items added via `Line.addData(...)` / `Item.addData` /
   `Item.addLine` cannot be tracked (`Line.addData` returns the `Line`,
   not the `Item`); a tracked `Line`'s contents can't be read back, so
   lines are opaque rows. Verified: both files compile with JDK 8
   `javac 1.8.0_504` against hand-written SDK stubs, plus a 36/36
   throwaway smoke test. **Still to integrate:** a new server → client
   WebSocket message type pushing `telemetryData()` /
   `telemetryLines()` snapshots, and the `opMode.tsx` panel below the
   stop button (additive `useTuner` slice). **Still unverified:** the
   internal-`TelemetryImpl` dependency at runtime against SDK 12.0.0
   (subclassing an internal class; fallback is a plain `Telemetry`
   interface wrapper) and the `update()` auto-clear assumption.
6. **JitPack publishing — DONE in files (not yet pushed, not yet built).**
   The conversion was done on 2026-11-11 after a first attempt was
   refused by a safety policy (retried on the user's ask, this time by
   editing the files directly). `Tuning/build.gradle.kts`: Dokka plugin
   + `dokkaPlugin` dependency + `dokkaJar` task + the entire `deployer`
   block (Sonatype Central Portal / GPG / nexus / local specs) removed;
   `maven-publish` added with the guide's Android-library publication
   block (`release`, groupId = project.group, artifactId `tuning`,
   version = project.version, `afterEvaluate { from(components
   ["release"]) }`); no `publishing.repositories` block (JitPack runs
   `publishToMavenLocal` itself). `gradle.properties`: Dokka flags
   removed; `GROUP=com.github.shlok-k720.AutoTune`,
   `VERSION_NAME=1.0.3` (group/version now read from those). `jitpack.yml`
   added at the repo root (JDK pin `openjdk17` only). **Wrapper
   downgraded 9.3.0 → 8.13** (see §10 for why — AGP 8.7.3 cannot run
   Gradle 9). Coordinate: `com.github.shlok-k720.AutoTune:tuning:1.0.3`.
   Java package / Android namespace (`com.pedropathing.tuning`)
   unchanged. **Still unverified:** no Gradle build on this VM; the
   first JitPack tag build is the real check, and the user must run
   `./gradlew publishToMavenLocal` on his Mac first. If JitPack cannot
   find the build (the Gradle project lives in `Tuning/`, one level
   down), the fallback is a `jitpack.yml` install override
   (`cd Tuning && ./gradlew build publishToMavenLocal`) — JitPack docs
   say a build file "can also be located in a subfolder", so the
   minimal file was tried first.
7. **README — DONE.** Top of `README.md` now states this is a fork of
   Pedro-Pathing/AutoTune and lists the added features in short.

## 0. Read this first — the fork, and three corrections

**Work in the fork.** Repo: **shlok-k720/AutoTune**, branch `main`, HEAD
`6a0b1c2` ("Add telemetry wrapper and opmode procedure", 2026-11-11).
Push target is the fork — **never** upstream Pedro-Pathing/AutoTune. The
fork is 3 commits ahead of upstream `60a332bf` (v1.0.3):
- `6612054` "gitign: ignore .idea" (new `.gitignore`, one line: `.idea`)
- `d62f3e3` "Change build and settings.gradle"
- `6a0b1c2` "Add telemetry wrapper and opmode procedure"

Corrections (v1 got these wrong because it surveyed upstream):
1. **`OpModeProcedure` existed in the fork — and has now been DELETED on
   main** (see "Status" above). It was a 22-line pure scaffold
   (`procedures/OpModeProcedure.java`, empty `run()`, TODO for an OpMode
   selector). The user confirmed it was standalone, and the code agreed:
   an exhaustive grep found exactly 3 references, all inside its own
   file. The file and the then-empty `procedures/` directory were
   removed; nothing else changed for the deletion. (One nuance that no
   longer matters: it carried `@Tuner`, so `TunerScanner` *may* have
   auto-discovered it — whether Sinister's `NarrowSearch` scanned the
   `procedures` subpackage was never verified.) Do **not** delete
   `Procedure.runOpMode` — that is the core execution primitive every
   procedure that runs a `TuningOpMode` depends on.
2. **A telemetry wrapper existed — and its interface is now COMPLETE**
   (see "Status" above). `TelemetryFinder.java` (194 lines) existed in the
   fork, unwired and half-stubbed. It is now finished at the interface
   level: `telemetryData()` returns tracked items, `inject()` is wired
   into `TuningOpMode.runOpMode()`, `restore()` undoes the swap. What
   is still missing is the transport + UI (new WebSocket message type
   and the `opMode.tsx` panel). See §5 for exactly what is there and
   what remains.
3. **Versions moved.** The fork compiles against FTC SDK **12.0.0** (was
   11.2.1) and revhub **3.0.2** (was 3.0.1), and the Gradle project name is
   now **`Tuning`** (capital T, was `tuning`). Version is still 1.0.3.
   The agent must answer the "Pedro telemetry" question against revhub
   3.0.2, and must verify the `TelemetryImpl` internal-API dependency (§5)
   against SDK 12.0.0 — it is **not compile-verified** (Gradle cannot run
   on this VM).

## 1. Repo and base state

- Repo: shlok-k720/AutoTune (fork of Pedro-Pathing/AutoTune), branch `main`,
  HEAD `6a0b1c2`. Version 1.0.3 (`gradle.properties` untouched).
- Robot side: `Tuning/` — Android library (Gradle). AGP 8.7.3, Kotlin 2.3.21,
  namespace `com.pedropathing.tuning`, compileSdk 35, minSdk 24, Java 1.8.
  compileOnly RobotCore / FtcCommon / RobotServer **12.0.0**. Implementation:
  `com.pedropathing:revhub:3.0.2`, FrozenMilk Sloth 0.3.2, jnanoid 2.0.0,
  nanohttpd-websocket 2.3.1. Root project name `Tuning`. No new dependencies
  in the fork.
- Java files in `Tuning/src/main/java/com/pedropathing/tuning/autotune/`
  (upstream's 15, plus the fork's 2):
  `Display`, `DisplayName`, `Hooks`, `ImageRegistrar`, `Inputs`,
  `InterruptibleBlock`, `Procedure`, `TelemetryFinder` (**new**), `Tuner`,
  `TunerRegistrar`, `TunerScanner`, `TuningOpMode`, `TuningSession`,
  `Utils`, `WebServer`, `WebSocketServer`; plus
  `procedures/OpModeProcedure` (**new, to be deleted**).
- Web side: `web-tuner/` — React 19.2.8 + TypeScript + Vite 8 + Tailwind 4.
  react-router 8.3.0, jotai (theme atom only), zod 4.4.3 (message validation),
  `reconnecting-websocket`, shiki (code highlighting), lucide-react icons.
  No component library, no state manager beyond jotai. Build:
  `tsc -b && vite build`. Gradle task `buildFrontend` (Bun) builds the
  web-tuner into `Tuning/src/main/assets/pedro`; `preBuild` depends on it.
  (`Tuning/src/main/assets/` does not exist in the checkout — it is a build
  output.) **The fork diff touches `web-tuner/` in 0 lines — every web
  finding below is verified against the fork (identical to upstream).**
  No tests in either module. No CI files.
- Package identity (do not rename): all Java files are
  `com.pedropathing.tuning.autotune` (plus the `procedures` subpackage, which
  disappears with the deletion); `Tuning/build.gradle.kts` group is
  `com.pedropathing`; web-tuner `index.html` title is "Pedro Pathing
  AutoTune"; license is BSD 3-Clause, Copyright (c) 2026, Pedro Pathing.

## 2. How the thing works today

### Web-tuner structure
- Routes (`web-tuner/src/main.tsx`): only two — `/` → `pages/home.tsx`,
  `/tuner/:id` → `pages/tuner.tsx`, both inside `pages/layout.tsx` (dark/light
  theme wrapper + a "Computer required" screen for small screens).
- **No tab structure exists at the top level.** The only tabs in the app are
  local to `components/screens/complete.tsx` (Values / Java / Kotlin / JSON
  result tabs, `useState<Tab>` + sliding underline) — copy that pattern for
  the new tab bar.
- `pages/home.tsx`: fetches `GET /api/tuners`, renders a 3-column card grid;
  each card links to `/tuner/${id}` with name + description. This is the
  procedures list.
- `pages/tuner.tsx`: renders `components/header.tsx` (logo, tuner name,
  E-STOP button, theme toggle) + one screen chosen by the `useTuner` state
  machine: `loading | confirmation | inputs | opModeRunning | complete |
  error`. **The tab bar belongs in `header.tsx` (or in `tuner.tsx` directly
  under it).**

### Server protocol
- HTTP (`WebServer.java`, NanoHTTPD, port **10158**). Only one API route:
  `GET /api/tuners` returns JSON `[{id, name, description}]` from
  `TunerRegistrar.getProcedures()`. Also: `GET /images/{id}` streams a
  registered image (`ImageRegistrar`), `/favicon.svg` and `/assets/*` serve
  static files from `assets/pedro/`, anything else serves `index.html`
  (SPA fallback).
- WebSocket (`WebSocketServer.java`, NanoWSD, port **12649**; the client
  connects to `ws://<hostname>:12649`, see
  `web-tuner/src/lib/tuningConnection.ts`). Message types (validated by
  `web-tuner/src/lib/schemas.ts`, `serverMessageSchema`):
  - Server → client: `init{name}`, `confirm{requestId, title, message,
    display?}`, `inputs{requestId, inputs, display?}`,
    `opModeRunning{requestId, canStop, name, display?}`,
    `complete{results, resultCode}`, `error{message}`.
  - Client → server: `init{id}`, `confirm{requestId}`,
    `inputs{requestId, values}`, `stopOpMode{requestId}`, `abort`.
- **Hard constraints that shape the tabs design:**
  - Exactly **one** active socket — a second browser gets `PolicyViolation:
    "A tuning client is already connected."`
    (`claim()` / `activeSocket`).
  - Exactly **one** `TuningSession` at a time (`currentSession` singleton;
    `beginProcedure` returns null if one is running).
  - A socket can run only **one** procedure per connection (`handleInit`
    errors if a session already exists: "Reload the page to start over.").
  - Consequence: the tabs must share one `TuningConnection` / one `useTuner`
    state. The OpMode-control and hardware-config tabs **cannot** open their
    own sockets or start their own sessions.

### How procedures run today
- `TunerScanner` (a FrozenMilk Sinister `Scanner`) finds `public static`
  zero-arg methods annotated with `@Tuner` (`Tuner.java`: `@Target(METHOD)`,
  `String name() default ""`) that return `Procedure`, invokes them, and
  registers the result in `TunerRegistrar` (id = 6-char nanoid,
  `Utils.nanoid()`).
- Clicking a card navigates to `/tuner/:id`; `useTuner` opens the socket and
  sends `{type:"init", id}`; the server calls
  `TuningSession.beginProcedure(id, transport)`, which starts the procedure
  on a thread named `"Pedro-Tuning"`. There is no "run" button — navigation
  is the run action. No search/filter, no history, no running two procedures;
  leaving the page aborts (header logo click and E-STOP both send `abort`).
- `Procedure.runOpMode(TuningOpMode<Result>)` (`Procedure.java:124`,
  `protected final`): asks for a confirmation screen, registers the OpMode
  under the fixed name `"Pedro Tuning"` (`OpModeMeta.Flavor.UTILITY`) via
  Sinister, inits/starts it through `Hooks.getOpModeManager()`
  (`OpModeManagerImpl.initOpMode` + `startActiveOpMode`), notifies the
  session (server sends `opModeRunning`), then blocks on
  `opMode.awaitResult()`; `finally` stops and unregisters it.
- `TuningSession.stopOpMode(requestId)` only calls
  `activeOpMode.requestOpModeStop()` when `activeOpMode.canStop` is true.
- The `Inputs` flow is the closest thing to a config editor today:
  `Procedure.inputs(title, description)` builds fields (`s()` / `d()` /
  `b()` / `i()` / `e()`), `awaitInputs(inputs)` blocks the procedure thread
  on a `CountDownLatch` until the browser submits; values are validated
  server-side in `Inputs.setValues` (all-or-nothing: convert + validate
  everything before assigning anything). Field model:
  `STRING / DOUBLE / BOOLEAN / INT / ENUM` + min/max/default + validation in
  `Inputs.Field.validate`. Input components live in
  `components/inputs/*` (`FieldInput.tsx`); zod `fieldSchema` mirrors them.

### Fork additions: TelemetryFinder and Hooks
- `Hooks.java` (commit `6a0b1c2`): exactly two visibility changes, no logic
  — `class Hooks` → `public class Hooks`, and `static OpModeManagerImpl
  getOpModeManager()` → `public static ...`. That is the whole change: it
  exposes the OpMode manager to other packages (the `procedures`
  subpackage and any user code). `TelemetryFinder` itself is in the same
  package so it did not strictly need this; `Procedure.java:89` already
  called it from the same package upstream.
- `TelemetryFinder.java` — see §5. It is the only new robot-side subsystem
  in the fork, and it is currently unwired: nothing outside its own file
  references it.

## 3. Task 1 — tab interface in the web-tuner (OpModeProcedure deletion DONE; tabs remain)

**Deletion DONE on main:** `procedures/OpModeProcedure.java` and the
then-empty `procedures/` directory were removed (verified zero
references). Do not touch `Procedure.runOpMode` or anything else for
this deletion.

Then add a tab bar at the top (in `header.tsx` or `tuner.tsx` directly
under it), copying the tab pattern from `components/screens/complete.tsx`
(local `useState` + sliding underline). Three tabs to start (a fourth is
defined in §8 — add it when its features land, not before):

1. **Procedures** — the existing `tuner.tsx` state machine, unchanged: the
   home card grid (`GET /api/tuners`), then confirmation → inputs →
   opModeRunning → complete per procedure.
2. **OpMode control (with telemetry)** — see §5/§8: list/run/stop OpModes,
   live telemetry panel, camera view. Build OpMode control on
   `Hooks.getOpModeManager()` (now public, §2) + the existing
   `TuningSession` stop path. Do **not** build it by wrapping OpModes in
   procedures — that was the deleted `OpModeProcedure` approach.
3. **Hardware config** — see §4/§8: the hardware-category config vars and
   the hardware-config XML editor.

Rules: one shared `TuningConnection` / `useTuner` state across all tabs
(§2 hard constraints — no second socket, no second session). Starting an
arbitrary OpMode from tab 2 while a procedure session is active conflicts
with the single-session rule — design around it (e.g. OpMode control is
only available when no procedure is running, or it takes over the session
explicitly; pick one and document it). Keep the header/tab bar minimal: put
secondary features behind the relevant tab, never in the header. Do not
clutter: a tab shows one concern; if a feature does not clearly belong in
tabs 1–3, it goes in the new **Diagnostics** tab (§8), not squeezed into
tab 2.

## 4. Task 2 — config variable interface + dashboard section

### The pattern to copy: the user's FTCUtils Variables
(Repo: shlok-k720/FTCUtils, `main` @ `89637b9`,
`core/src/main/java/com/rmsftc/utils/config/vars/`, pure JVM, Java 1.8.
This is the user's own design and the names below match what he asked for.
Note: the FTCUtils `ftc/.../DashboardVars.java` helper was **deleted by the
user** in commit `ed0a540` — there is currently no dashboard integration in
FTCUtils; the registry is the enumeration source and the upload wiring must
be written fresh here.)

```java
// Declare
new Var<>(0.1)                                              // unnamed, field-backed
new Var<>(provider)                                         // unnamed, delegates to a VarProvider
new Var<>("Max speed", 0.8, Validator.inRange(0.0, 1.0))    // named
new Var<>("Alias", provider, Validator.nonnegative())       // named, provider-backed
// Read/write: Var<T> implements VarProvider<T>; Val<T> is the read-only counterpart
// (constant or live view). get() reads field or provider.
// set(T) enforces LOCK first, then VALIDATION, then writes.
//   - makeFinal() / makeMutable() / isMutable(); set() throws
//     IllegalStateException when locked.
//   - Validation<T> holds an unmodifiable Set<Validator<T>>; validate()
//     throws IllegalArgumentException listing every failed validator.
//   - Validator factories: positive(), nonnegative(), negative(),
//     nonpositive(), nonnull(), inRange(min, max), nonZero().
//   - Quirk: the constructor's initial value is NOT validated; only set() is.
// Modifier: var.at(value) captures the original at construction;
//   apply() / revert() / getOriginal() / getValue() / applied() / getVar().
//   Both go through Var.set, so lock + validation still apply.
// Grouping: Section { String category, name; boolean isModifier; },
//   VARIABLES_CATEGORY = "Variables", MODIFIERS_CATEGORY = "Modifiers".
//   One category level, no nesting.
// Registry: Variables holds static synchronized Set<Var<?>> variables and
//   Set<Modifier<?>> modifiers, with add(), addModifier(),
//   finalizeVariable(var) (clears its modifiers, nulls + locks it via
//   GarbageCollector, removes it). Named vars self-register on construction.
// Kotlin: DoubleVar / IntVar / StrVar / BoolVar typealiases + inline
//   factories in config.vars.kt (no PosVar — no Pose2d in that library).
// Config framework (separate): Configuration<T> / IConfiguration<T> /
//   Configurator<T> — defaults() then a configurator lambda overrides.
//   Trap: defaults() runs during the superclass constructor, before subclass
//   field initializers.
```

### Take two things from Pedro, one from UtilLib — copy nothing else from them
- **Pedro Pathing `ConfigVars`** (`com.pedropathing.config`, v3.0.2,
  5 files; no dashboard integration anywhere in Pedro, 0 hits for
  `FtcDashboard`): factory vocabulary `ConfigVar.of(value, validators...)`
  (validated default) vs `required(...)` vs `ofNullable(...)` — adopt this,
  because it fixes FTCUtils' gap where the constructor's initial value is
  never validated. And Pedro's `Configuration<T>` lambda pattern: group
  tuner constants into config classes (`MecanumConfig`-style) whose ctor
  takes a `configure` lambda; the config class becomes the dashboard
  section. `ConfigVar<T> implements Supplier<T>`; `get()` throws
  `IllegalStateException` if never set, then validates and returns.
  Do **not** copy Pedro's asymmetry (`set()` does not validate, only the
  ctor and `get()` do) or its nameless/unregistered design.
  Pedro's `Validator` has only `positive()`, `nonnegative()`, `negative()`,
  `nonpositive()`, `nonnull()` (no `inRange` / `nonZero`).
- **UtilLib vars** (`com.rmsftc.vars`, main `4bb5e02`): do **not** copy it.
  Its `Variable.set()` never checks its declared validators (dead code), its
  `Caption` (name/category) is unused anywhere, its `Modifier.getOriginal()`
  misleadingly returns the provider instead of the original value, and its
  `VariableManager` registry is write-only (registered providers cannot be
  enumerated from outside). The **one** feature worth taking is its
  `onSet` / `onGet` `Runnable` hooks — the change listeners FTCUtils lacks,
  useful for a dashboard section that re-renders on change.
- **Gaps no reference system closes** (the agent must design these):
  (1) the dashboard transport — none of the three systems talks to a
  dashboard; AutoTune needs its own registry → JSON → editor bridge
  (FTC Dashboard's equivalent is `addConfigVariable(category, name,
  ValueProvider)` bridging `get()` / `set()`); (2) no persistence in any
  system — values are session-only; (3) no type metadata — editors must
  branch on the runtime value type (`Double` / `Int` / `Boolean` /
  `String` / enum) since all three systems are plain generics.

### What to build
- **DONE (interface, on main):** a robot-side config-variable registry
  in `com.pedropathing.tuning.autotune.config` — `Var`, `Val`,
  `ValGetter`, `ValSetter`, `VarProvider`, `Validation`, `Validator`
  (all 7 factories, null-safe), `Modifier`, `Variables` (enumerable
  registry), `Section`, `GarbageCollector`, `ConfigVar` (Pedro `of` /
  `required` / `ofNullable` factories), `Configuration` /
  `Configurator` / `IConfiguration` (lambda grouping), `kt/Types.kt`
  (DoubleVar/IntVar/StrVar/BoolVar), `package-info.java`. Semantics
  per the FTCUtils port (§4 above), with two documented deviations:
  numeric validators are null-safe pure predicates; `Validation` uses
  `LinkedHashSet` and `ConfigVar.of()` puts the implicit `nonnull()`
  first. Verified: `javac --release 8` + kotlinc 2.1.20 exit 0, plus a
  throwaway 41-assertion check, 41/41 pass.
- **Still to integrate (transport):** a transport channel over the
  existing WebSocket (new message types, added — never changed, §10) or
  a new HTTP endpoint for get/set; reuse the `Inputs.Field` model and
  `components/inputs/*` + zod `fieldSchema` for the editors (a
  config-variable system is essentially "persistent `Inputs.Field`s with
  a get/set channel instead of a one-shot submit").
- **Still to integrate (UI):** a dashboard section for editing config
  variables: in tab 2 for the global set (grouped by section/category),
  and in tab 1 per-procedure where a procedure declares vars. Validation
  errors from `set()` must surface in the UI. Save sends only changed +
  valid values (FTC Dashboard behavior).

## 5. Task 3 — telemetry injection (interface COMPLETE; transport + UI remain)

### What exists now: TelemetryFinder (completed on main)
`Tuning/src/main/java/com/pedropathing/tuning/autotune/TelemetryFinder.java`
(package `com.pedropathing.tuning.autotune`):

```java
public class TelemetryFinder {
    public static TelemetryFinder getInstance() // thread-safe (holder idiom)
    public TelemetryImpl findTelemetry()        // null-safe: null when no active OpMode or not a TelemetryImpl
    public void inject()                        // total + idempotent; no-ops when nothing can be injected
    public void restore()                       // puts the original telemetry back; never clobbers
    public boolean isInjected()
    public Telemetry.Item[] telemetryData()     // snapshot array of tracked items
    public List<Telemetry.Item> getItems()      // defensive copy
    public Telemetry.Line[] telemetryLines()    // snapshot array of tracked lines
    public List<Telemetry.Line> getLines()      // defensive copy
}
```

`LogTelemetry` (package-private class in the same file, `extends
TelemetryImpl`) wraps a source `TelemetryImpl`: all four `addData` overloads
delegate to `source.addData(...)` and record the returned `Item` in a
`CopyOnWriteArrayList<Telemetry.Item> items`; `addLine` / `addLine(String)`
record the `Line` in a `lines` list; `removeItem` / `removeLine` remove
from the tracked lists as well as delegating; `clear()` drops only
non-retained entries (mirroring `TelemetryImpl.clear()` — retained items
survive), `clearAll()` drops everything, `update()` mirrors auto-clear
after transmit when `isAutoClear()` is on. Everything else is a pure
delegate to `source` (`addAction`, `removeAction`, `speak` (×2),
`isAutoClear`/`setAutoClear`, transmission-interval / separator getters
and setters, `setDisplayFormat`, `log()`).

**Wiring (done):** `inject()` is called at the top of
`TuningOpMode.runOpMode()` (final — the single choke point) before
`runTuningOpMode()`, and `restore()` in the `finally` (before
`finished.countDown()`). Both calls are wrapped in
`try/catch (RuntimeException)` so tracking can never take down a tuning
procedure. `WebSocketServer.java`, `TuningSession.java`, and
`Procedure.java` are untouched.

**Known limits (documented in the file's class KDoc):** items added via
`Line.addData(...)` / `Item.addData` / `Item.addLine` cannot be tracked
(`Line.addData` returns the `Line`, not the `Item`, so the wrapper is
never handed the created item) — OpModes should use top-level
`telemetry.addData(...)` for visible data. A tracked `Line`'s contents
can't be read back through the public API, so lines are exposed as
opaque rows.

### What remains (transport + UI)
1. Add a **new** server → client WebSocket message type (e.g.
   `telemetry{lines}`) and push `telemetryData()` / `telemetryLines()`
   snapshots over it (or a polling HTTP endpoint). Follow FTC
   Dashboard's packet shape as the model:
   `TelemetryPacket{timestamp, data: SortedMap<String,String>, log:
   List<String>, field, fieldOverlay}` — latest value per key, cleared on
   OpMode init. `isInjected()` gates reads.
2. Render it in the UI (see §7 for the panel placement).
- **Caveats the agent must respect:**
  - `LogTelemetry` extends `TelemetryImpl`, an **internal** SDK class
    (`org.firstinspires.ftc.robotcore.internal.opmode`) — an internal-API
    dependency that can break across SDK versions. The fork bumped the
    SDK to **12.0.0**: **verify this compiles and runs against SDK
    12.0.0 before building on it** (not compile-verified here — Gradle
    cannot run on this VM; the completion was compile-checked with JDK 8
    against hand-written SDK stubs plus a 36/36 throwaway smoke test).
    If it breaks, the fallback is a plain `Telemetry` interface wrapper
    instead of subclassing `TelemetryImpl`.
  - One assumption baked into `LogTelemetry.update()`: that
    `TelemetryImpl.update()` clears non-retained entries when auto-clear
    is on (so the tracker mirrors it). Verify against SDK 12.0.0
    sources; if `update()` does not clear, the tracker under-reports
    after the first update; if it clears even when throttled, the
    tracker briefly over-reports. Either failure is transient (next
    update reconciles).
- **Open question, now narrower:** the user chose the wrap-the-SDK-
  `TelemetryImpl` approach, so "what Pedro telemetry is" is answered for
  the wrapper itself — but whether that wrapper survives SDK 12.0.0 at
  runtime is still unverified, and the agent must still check revhub 3.0.2
  for a Pedro telemetry helper before assuming there is none.
- UI rule: telemetry updates must be **additive** — a separate state slice
  in `useTuner`, not a new screen state — because `useTuner`'s state machine
  replaces the whole screen per message and a telemetry frame must not
  re-render/replace the screen.

## 6. Task 4 — camera Display mode (Limelight / standard webcam)

`Display.java` — `public abstract class Display` with a private constructor
and two nested subtypes, created only via static factories:

```java
Display.image(ImageRegistrar.Handle lightMode, ImageRegistrar.Handle darkMode)
Display.image(ImageRegistrar.Handle handle)          // same handle for both themes
Display.fourWheelBot(FourWheelBot.Wheel wheel, boolean reversed)
// FourWheelBot.Wheel: FRONT_LEFT, FRONT_RIGHT, BACK_LEFT, BACK_RIGHT
```

- `Display.Image` holds two `ImageRegistrar.Handle`s (light/dark). Images are
  registered via `ImageRegistrar.registerPNG/JPEG/SVG(Callable<InputStream>)`
  / `registerAssetPNG/JPEG/SVG(String path)` and served at `/images/{id}`.
- `Display.FourWheelBot` holds a `Wheel` + `reversed` boolean.
- Selection: a procedure sets the display for the next step with
  `Procedure.withDisplay(Display, InterruptibleBlock)` (scoped; restores the
  previous display in `finally`). `currentDisplay` is attached to whatever
  the procedure requests next (confirmation / inputs / opModeRunning). There
  is **no global/default display** — `currentDisplay` is null unless a
  procedure sets one, and all three screens render a no-display variant.
- Serialization: `WebSocketServer` registers a Gson
  `RuntimeTypeAdapterFactory.of(Display.class, "type")` with subtypes
  `"image"` and `"fourWheelBot"`. The frontend mirrors it exactly in
  `web-tuner/src/lib/schemas.ts` (`displaySchema`, a zod discriminated union
  on `type`; `imageHandleSchema = z.string().length(6)` matching the 6-char
  nanoid).
- Rendering: `web-tuner/src/components/display/display.tsx` switches on
  `definition.type` → `image.tsx` (two `<img src=/images/{handle}>`,
  light/dark via Tailwind `dark:` classes) or `fourWheelBot.tsx` (a CSS-3D
  interactive robot: drag to rotate, the selected wheel spins, `reversed`
  flips direction; artwork in `web-tuner/src/assets/four-wheel-bot/*.svg`).
  `displaySplit.tsx` lays out display (left, flexible) + content panel
  (right, fixed 24rem) for the confirmation/inputs/opMode screens.

Adding a camera mode requires, in order: (1) a new nested `Display`
subtype + static factory in `Display.java` (e.g.
`Display.camera(...)`); (2) a `registerSubtype(..., "<type>")` line in
`WebSocketServer`'s Gson factory; (3) a new variant in `displaySchema` in
`schemas.ts`; (4) a new case in `display.tsx` + a new renderer component
(e.g. `camera.tsx`); (5) a server-side source for the frames — this is the
hard part:
- **Standard webcam:** frames come from the robot (VisionPortal) as an MJPEG
  stream or via a `sendImage`-style path — the agent must check the
  VisionPortal API in the user's robot code; no camera code exists in this
  repo. FTC Dashboard's model: `startCameraStream(CameraStreamSource,
  maxFps)` or `sendImage(Bitmap)`; frames JPEG-compressed (quality default
  50) and base64'd into a `RECEIVE_IMAGE`-style message; client draws
  `data:image/jpeg;base64,...` to a canvas (rotatable 0/90/180/270°).
- **Limelight:** Limelight is a separate device with its own web UI and
  stream — proxy/embed it, do not re-implement it. FTC Dashboard's model:
  a `startCameraStream(Limelight3A, maxFps)` overload, plus a
  `LimelightProxyManager` that forwards the Limelight at `172.29.0.1` to the
  Hub's Wi-Fi address at the same ports — 5800 MJPEG, 5801 web UI, 5805 WS,
  5807 REST. The client polls `http://<host>:5807/status` every 2 s,
  streams `http://<host>:5800`, and links `http://<host>:5801`.
- One mode must cover both sources (a source selector inside the camera
  Display, not two Display subtypes), and it sits alongside `image` and
  `fourWheelBot` as a third `Display` subtype.

## 7. Task 5 — telemetry in TuningOpMode

Same feed as §5. Render the panel in
`web-tuner/src/components/screens/opMode.tsx` below the stop button
(`OpModeContent` — the stop button is shown only when `canStop`; while
stopping it shows "Stopping…" and disables itself via `stopRequested` state
in `useTuner.stopOpMode()`). Attach the feed to the `opModeRunning` state
in `useTuner.ts` as an additive slice (§5). `TuningOpMode` itself
(`TuningOpMode.java`):

```java
public abstract class TuningOpMode<Result> extends LinearOpMode {
    public final String name, description;
    public final boolean canStop;
    public TuningOpMode(String name, String description, boolean canStop)
    protected abstract Result runTuningOpMode() throws InterruptedException;
    // runOpMode() is final: calls runTuningOpMode(), captures failure, counts down `finished`
    final Result awaitResult() throws InterruptedException;  // rethrows failure, else returns result
}
```

It already **has** the standard `telemetry` field (it extends
`LinearOpMode`) — and the fork's `TelemetryFinder.inject()` is now wired
into `TuningOpMode.runOpMode()` (before `runTuningOpMode()`,
`restore()` in the `finally`; both `try/catch`-guarded — see §5).
What remains is the transport + panel: add the WebSocket message type
and render the panel. `TuningOpMode.java` is otherwise untouched. Results flow is unchanged:
`runTuningOpMode()` returns `Result` → `awaitResult()` →
`Procedure.runOpMode()` returns it to the procedure → the procedure stores
user-facing values via `Procedure.result(name, value)` / `code(language,
code)` → server sends `complete{results, resultCode}` →
`screens/complete.tsx` renders the values table + code tabs. Do not change
that flow; telemetry is an additional live channel, not a replacement.

## 8. Task 6 — FTC Dashboard feature map (implement in this order)

Source: acmerobotics/ftc-dashboard, default branch `master`, HEAD `a876f09`,
dashboard version 0.6.0. Architecture the port reproduces: `DashboardCore`
(transport-agnostic pure Java: config tree, telemetry batching default
100 ms / max 100 queued packets, `SocketHandler` / `SendFun`) +
`FtcDashboard` (Android/FTC wrapper: serves the web client on port **8080**
at `/dash` via the RC's own `WebServer`, WebSocket server on port **8000**).
Protocol: JSON over WebSocket, one message per Redux action; incoming
server messages dispatch straight into the Redux store. Heartbeat: client
sends `GET_ROBOT_STATUS` every 1 s; server replies `RECEIVE_ROBOT_STATUS`
(also used to measure ping). All 20 message types, from
`message/MessageType.java` — client → server: `GET_ROBOT_STATUS`,
`INIT_OP_MODE`, `START_OP_MODE`, `STOP_OP_MODE`, `GET_CONFIG`,
`GET_CONFIG_BASELINE`, `SAVE_CONFIG`, `RECEIVE_GAMEPAD_STATE`,
`SET_HARDWARE_CONFIG`, `WRITE_HARDWARE_CONFIG`, `DELETE_HARDWARE_CONFIG`;
server → client: `RECEIVE_ROBOT_STATUS`, `RECEIVE_OP_MODE_LIST`,
`RECEIVE_CONFIG`, `RECEIVE_CONFIG_BASELINE`, `RECEIVE_TELEMETRY`,
`RECEIVE_IMAGE`, `RECEIVE_HARDWARE_CONFIG_LIST`, `RECEIVE_LOGCAT_ERRORS`;
either: `RECEIVE_GAMEPAD_STATE`. Web client: React 18.2 + Redux 4.2 +
redux-thunk, Vite 3, TypeScript 4.8, Tailwind 3, `react-grid-layout` for the
free-form layout; one Redux slice per domain; middleware: `socketMiddleware`
(WebSocket + heartbeat), `gamepadMiddleware`, `storageMiddleware` (persists
only the layout preset in localStorage).

Tab key: **1** Procedures · **2** OpMode control · **3** Hardware config ·
**NEW "Diagnostics"** · **Chrome** (global header/settings, not a tab).

| Feature | Tab | What it is / how it works | Support |
|---|---|---|---|
| OpMode list & control | 2 | Dropdown of all OpModes grouped by group; state-aware buttons: stopped → Init only; INIT → Start + Stop; RUNNING → Stop only. Client dispatches `INIT_OP_MODE` / `START_OP_MODE` / `STOP_OP_MODE` with `opModeName`; server implements `OpModeManagerImpl.Notifications`, list built once from `RegisteredOpModes` (excludes SYSTEM flavor, sorted by group then name) and pushed as `RECEIVE_OP_MODE_LIST`. | Robot-side |
| Robot status + heartbeat | 2 (status strip) | Header: connection icon, ping (ms), battery voltage, OpMode status INIT/RUNNING/STOPPED, warning/error strings. `RECEIVE_ROBOT_STATUS{enabled, available, activeOpMode, activeOpModeStatus, warningMessage, errorMessage, batteryVoltage}`; 1 s heartbeat. | Robot-side |
| Telemetry display | 2 | Live key–value lines + log area; latest value per key, cleared on OpMode init. Server batches `TelemetryPacket`s → `RECEIVE_TELEMETRY`; robot API `sendTelemetryPacket()` or `getTelemetry()` (restricted `Telemetry`; `update()` sends a packet); `MultipleTelemetry` fans out to DS + dashboard. | Robot-side |
| Telemetry graph | 2 | Plots selected numeric telemetry keys over a configurable time window; play/pause; key picker. Pure client: reads the telemetry store, no new messages. Needs numeric values in telemetry packets. | Pure web |
| Field view | 2 | Field image with a canvas overlay drawn from telemetry (robot pose, paths, shapes, text, images, grids); custom origins/rotation/scale. Robot draws into `packet.fieldOverlay()` (a `Canvas` recording ops); ops travel inside `TelemetryPacket`; client replays them over the field image. Dashboard's default field is `/dash/decode.webp` 144×144 in + 7×7 grid (season-specific — AutoTune needs its own). | Both |
| Config variables (@Config editing) | 1 | Live-editable tuning constants, grouped in collapsible categories; text/number/boolean/enum inputs with validation; save sends only changed + valid values; filter toggle shows only values differing from baseline. Robot: `@Config` on a class makes all public static non-final fields config vars, or code calls `addConfigVariable(category, name, ValueProvider)`; types boolean/int/long/float/double/string/readonly_string/enum/custom nested objects; server pushes the tree as `RECEIVE_CONFIG` on connect and on change; client edits locally, Save sends `SAVE_CONFIG` with the diff; `GET/RECEIVE_CONFIG_BASELINE` captures startup values for the modified filter. **In AutoTune this is §4, not Dashboard's `@Config` reflection** — use the FTCUtils-Variables registry instead. Show per-procedure in tab 1, global set in tab 2. | Both |
| Hardware-category config vars | 3 | Same editor, but for the special `__hardware__` category: live motor/servo values. `FtcDashboard.HARDWARE_CATEGORY = "__hardware__"`; values surface from the built-in HardwareOpMode (see below). | Robot-side |
| Camera stream | 2 | Live camera frames in a rotatable canvas (0/90/180/270°). See §6 for the AutoTune shape (Display subtype + frame source). | Robot-side |
| Limelight view | 2 | Limelight status (temp/CPU/RAM/pipeline), MJPEG stream, link to the Limelight web UI. See §6 (proxy ports 5800/5801/5805/5807). | Robot-side (proxy) + pure-web view |
| Gamepad input | 2 — **defer** | Drive the robot from the browser: physical USB gamepads (Browser Gamepad API) or on-screen virtual sticks/buttons/sliders, or keyboard mapping; Start+A claims gamepad 1, Start+B claims gamepad 2. Client polls `navigator.getGamepads()` every animation frame, 0.2 deadzone + SDK-style rescaling, sends `RECEIVE_GAMEPAD_STATE{gamepad1, gamepad2}`; server copies into SDK `Gamepad` objects; 500 ms watchdog zeroes them if updates stop. **Dashboard's own docs warn: higher latency, less robust than a real DS; use only for testing.** Highest risk, lowest value for a tuner — implement last or not at all. | Both |
| Hardware config editor | 3 | List, select, edit (text or GUI), save-as, and delete the robot's hardware configuration XML files; shows which config is active and read-only flags. Server reads `RobotConfigFileManager` XML files → `RECEIVE_HARDWARE_CONFIG_LIST{configs[{name, xmlContent, readOnly}], activeConfig}`; client sends `SET_HARDWARE_CONFIG` / `WRITE_HARDWARE_CONFIG` / `DELETE_HARDWARE_CONFIG`. | Robot-side |
| HardwareOpMode live hardware control | 3 | A built-in `@TeleOp(name="Hardware", group="Dashboard")` that auto-discovers motors/servos in the hardware map and exposes live power/position/current/port info for direct control and monitoring. Values surface as config vars under `__hardware__`, rendered by the hardware view. | Robot-side |
| Error / logcat view | NEW "Diagnostics" | Scrolling list of robot errors (OpModeManager tag only), auto-scroll toggle, clear button. Server tails logcat and pushes `RECEIVE_LOGCAT_ERRORS`; client filters to that tag, renders with timestamps. | Robot-side |
| Logging view (telemetry history + CSV export) | NEW "Diagnostics" | Timestamped table of all past telemetry values, per-key show/hide, CSV download. Pure client: accumulates the telemetry store into a local table; download exports CSV. No new messages. Cheap once the telemetry feed exists. | Pure web |
| Recorder / replay | NEW "Diagnostics" | Record up to 10,000 telemetry samples (~2.5 min), save named replays to localStorage (0.2 MB each, 5 MB total), replay field overlays onto the Field view at adjustable opacity, replay-on-start option. Pure client: ring buffer + `localStorage` keys `field_replay_*`; replay dispatches `setReplayOverlay`, and FieldView merges replay ops with live telemetry ops. | Pure web (replay feeds tab 2's field view) |
| Dashboard enable/disable + auto-enable | Chrome (global) | Master on/off for the whole dashboard (in-app OpMode + RC menu item); auto-enable remembered across restarts. When disabled the server swallows every message except `GET_ROBOT_STATUS`. In AutoTune this is a global concern (server start/stop), not a tab. | Robot-side |
| Layout presets + custom layout | Chrome | Header dropdown switching whole-page layouts (Default / Field / Graph / Hardware / Original / Custom free-form drag/resize grid of any views); preset choice persists in localStorage. In a 3-tab UI this **is** the tab system itself, plus an optional per-tab preset switcher — do not invent a tab for it. | Pure web |
| Settings (theme) + header status | Chrome (header/settings) | Settings modal: accent color + dark/light mode; header shows connection icon, ping, battery voltage. Global header above the tabs. | Pure web (battery value is robot-side data) |

### The new tab: "Diagnostics"
Three features belong in none of tabs 1–3, so they go to a new tab rather
than "none": **Error/logcat view, Logging view (history + CSV export),
Recorder/replay.** Why it needs its own tab: none of the three tabs is about
reviewing past data or robot logs. Tab 2 is *live* control + *live*
telemetry; the error console, the telemetry history table with CSV export,
and record/replay are all retrospective — you look at them after (or
independent of) a run, not while driving. Cramming them into tab 2 would do
exactly what the user forbade: clutter the live-control tab. One cross-tab
link: replay overlays feed tab 2's field view, so the replay player needs a
"show on field" affordance even though it lives in Diagnostics.

### Implementation order for task 6
Telemetry → config variables → OpMode controls → camera → field view →
Diagnostics (logcat/logging/replay) → graphs → gamepad (last, or not at
all). Do not start a later item before the earlier ones work end-to-end.

## 9. Task 7 — later
Out of scope for this brief. Do not design or build anything for it.

## 10. Hard constraints (non-negotiable)

- **Base and push target: the fork.** Branch `main` @ `6a0b1c2`,
  shlok-k720/AutoTune. Push to the fork — never to upstream
  Pedro-Pathing/AutoTune. Compile against FTC SDK **12.0.0** and revhub
  **3.0.2**; Gradle project name is **`Tuning`** (capital T) — any script,
  CI, or doc referencing the `:tuning` project path or the published
  artifact name must be checked for the new casing.
- **No API breakage.** The WebSocket protocol is versioned only by the zod
  `serverMessageSchema` — new message types must be *added* (optional
  fields / new `type` literals), never change existing ones
  (`confirm` / `inputs` / `opModeRunning` / `complete` / `error` / `init`
  shapes are the public API for any client). `Display`'s Gson `"type"`
  discriminator likewise only gains subtypes. `Procedure`'s protected
  methods (`confirmation`, `inputs`, `awaitInputs`, `runOpMode`,
  `withDisplay`, `result`, `code`) and `TuningOpMode`'s constructor /
  `runTuningOpMode` are the public authoring API — do not change their
  signatures.
- **Single socket, single session** (§2). One `TuningConnection`, one
  `useTuner` state, shared across tabs. No second socket, no second session,
  no second procedure per connection.
- **No clutter.** One concern per tab. Secondary features live behind their
  tab, never in the header. If a feature fits nowhere, it gets its own tab
  (Diagnostics) — it does not get squeezed into tab 2. Prefer the existing
  components (`components/inputs/*`, the `complete.tsx` tab pattern, the
  `displaySplit.tsx` layout) over new chrome.
- **Follow the repo's existing patterns.** New robot-side registries mirror
  `TunerRegistrar`; new WebSocket message types mirror the existing
  `serverMessageSchema` / `WebSocketServer.isKnownType` shapes; new Display
  subtypes follow the §6 five-step checklist; new config vars follow §4.
- **No new dependencies without a reason stated in the code.** The Gradle
  file already has revhub 3.0.2, Sloth, jnanoid, nanohttpd-websocket; the
  web-tuner already has React, zod, jotai, Tailwind. Do not add FTC Dashboard
  as a dependency to get its features — this brief ports the *features*, not
  the library (and AutoTune's server is its own NanoHTTPD/NanoWSD pair on
  ports 10158/12649, not Dashboard's 8080/8000).
- **Verify the fork's new code first.** `TelemetryFinder` / `LogTelemetry`
  and the new `config/` package have no Gradle build verification (Gradle
  cannot run on this VM); they were compile-checked by hand (`javac
  --release 8` / kotlinc for `config/`, JDK 8 `javac` against SDK stubs
  for the telemetry files, plus throwaway 41- and 36-assertion checks).
  Before building on `TelemetryFinder`, verify it compiles and runs
  against SDK 12.0.0; the internal-`TelemetryImpl` dependency is the
  known risk (§5). `OpModeProcedure` is deleted (see "Status").
- **JitPack conversion is DONE in files (not yet pushed or built).**
  `Tuning/build.gradle.kts` now uses `maven-publish` with the guide's
  Android-library publication block (group
  `com.github.shlok-k720.AutoTune`, version 1.0.3, artifact `tuning`);
  no `publishing.repositories` block; `jitpack.yml` pins `openjdk17` at
  the repo root. **Wrapper is Gradle 8.13, not 9.3.0** — AGP 8.7.3
  cannot run Gradle 9 (its compatibility table lists Gradle 8.9 as the
  minimum and Gradle 9 is tested only with AGP 9.0+; AGP enforces a
  hard Gradle-version check at configuration start). 8.13 is the
  recommended Gradle for AGP 8.7.x and sits inside Kotlin 2.3.21's
  supported range (Gradle 7.6.3–9.3.0, AGP 8.2.2–9.0.0). The guide's
  8.9 pin is the AGP 8.7 minimum; 8.13 was chosen over it as the top of
  the AGP 8.7 line. The Java package / Android namespace
  (`com.pedropathing.tuning`) did NOT change. Still unverified: no
  Gradle build on this VM; run `./gradlew publishToMavenLocal` on a Mac
  first, then the first JitPack tag build is the real check. If JitPack
  cannot find the build (the Gradle project lives in `Tuning/`, one
  level down — JitPack docs say a build file "can also be located in a
  subfolder"), the fallback is a `jitpack.yml` install override
  (`cd Tuning && ./gradlew build publishToMavenLocal`).

## 11. Open questions the agent must resolve before designing (do not guess)

1. Does the `TelemetryFinder` / `LogTelemetry` wrapper (subclassing internal
   `TelemetryImpl`) compile and run against FTC SDK 12.0.0? If not, the
   fallback is a plain `Telemetry` interface wrapper. Also check revhub
   3.0.2 for a Pedro telemetry helper before assuming there is none.
2. ~~Whether Sinister's `NarrowSearch` actually scans the `procedures`
   subpackage~~ — MOOT: `OpModeProcedure` and the `procedures/` directory
   were deleted on main (see "Status").
3. How Limelight vs standard webcam frames should be sourced on the robot
   side (VisionPortal API / Limelight stream URL scheme — check the user's
   robot code, not this repo; no camera code exists here).
4. Whether the OpMode list is re-pushed to late-joining clients (Dashboard
   sends `RECEIVE_OP_MODE_LIST` once after registration; the re-send on
   connect was not verified) — relevant if tab 2 lists OpModes.
5. `SAVE_CONFIG` diff handling for nested objects/arrays (flat
   primitives/enums are confirmed; nested behavior was not verified).

## 12. References (read before writing code)

- AutoTune fork (local clone): `Tuning/src/main/java/com/pedropathing/tuning/autotune/`
  — `WebServer.java` (HTTP routes), `WebSocketServer.java` (WS protocol,
  Gson Display factory), `TuningSession.java` (session + OpMode flow),
  `Procedure.java` (authoring API), `TuningOpMode.java`, `Display.java`,
  `ImageRegistrar.java`, `Inputs.java`, `TunerRegistrar.java`,
  `TunerScanner.java`, `Hooks.java`, `TelemetryFinder.java` (completed
  interface — see §5), `config/` (config-variable interface — see §4;
  `Var`, `ConfigVar`, `Validation`, `Validator`, `Modifier`,
  `Variables`, `Section`, `GarbageCollector`, `Configuration`,
  `kt/Types.kt`). `procedures/OpModeProcedure.java` was deleted on main.
  `web-tuner/src/` — `main.tsx`, `pages/home.tsx`, `pages/tuner.tsx`,
  `components/header.tsx`, `components/screens/complete.tsx` (tab pattern),
  `components/screens/opMode.tsx`, `components/display/display.tsx`,
  `components/display/displaySplit.tsx`, `components/inputs/*`,
  `lib/schemas.ts`, `lib/tuningConnection.ts`, `hooks/` (`useTuner`).
- FTCUtils Variables: shlok-k720/FTCUtils @ `89637b9`,
  `core/src/main/java/com/rmsftc/utils/config/vars/` (`Var`, `Validation`,
  `Validator`, `Modifier`, `Variables`, `Section`, `GarbageCollector`),
  `config.vars.kt/Types.kt`, `config/` (`Configuration`, `Configurator`,
  `IConfiguration`).
- Pedro ConfigVars: `com.pedropathing.config` in Pedro-Pathing/PedroPathing
  v3.0.2 (`ConfigVar`, `Configuration`, `Memoize`, `Validator`).
- FTC Dashboard: acmerobotics/ftc-dashboard @ `master` `a876f09`
  (v0.6.0) — `DashboardCore/.../DashboardCore.java`,
  `FtcDashboard/.../FtcDashboard.java`, `message/MessageType.java`,
  `telemetry/TelemetryPacket.java`, `config/Config.java`,
  `config/variable/VariableType.java`, `client/src/components/Dashboard/Dashboard.tsx`,
  `client/src/components/views/`, `client/src/store/`.
