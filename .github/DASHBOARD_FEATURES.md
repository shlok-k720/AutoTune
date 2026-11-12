# FTC Dashboard features — what to implement in AutoTune, and how

Source: acmerobotics/ftc-dashboard, `master` @ `a876f09` (v0.6.0). This file
is the implementation plan for task 6 of the web-tuner work. The full
feature map (with the tab assignment) also lives in
`.github/AGENT_CONTEXT.md` §8 — this file is the per-feature build list.

Dashboard's architecture, for reference: `DashboardCore` (transport-agnostic
pure Java: config tree, telemetry batching default 100 ms / max 100 queued
packets) + `FtcDashboard` (Android/FTC wrapper: serves the web client on
port 8080 at `/dash`, WebSocket on port 8000). Protocol: JSON over WebSocket,
one message per Redux action; heartbeat `GET_ROBOT_STATUS` every 1 s →
`RECEIVE_ROBOT_STATUS` (also the ping). **Do not port the library** — port
the features onto AutoTune's own server (NanoHTTPD port 10158, NanoWSD port
12649, one socket / one session — see AGENT_CONTEXT.md §2).

Tab key: **1** Procedures · **2** OpMode control · **3** Hardware config ·
**Diagnostics** (new tab) · **Chrome** (global header/settings, not a tab).

Implementation order (do not start a later item before the earlier ones
work end-to-end): telemetry → config variables → OpMode controls → camera →
field view → Diagnostics → graphs → gamepad (last, or not at all).

## 1. Telemetry display (tab 2) — first

What it is: live key–value lines + a log area; latest value per key,
cleared on OpMode init.

- Robot: add a server → client WebSocket message type (e.g.
  `telemetry{lines}`) carrying the tracked items from
  `TelemetryFinder.telemetryData()` (see AGENT_CONTEXT.md §5). Follow
  Dashboard's packet shape: `TelemetryPacket{timestamp, data:
  SortedMap<String,String>, log: List<String>, field, fieldOverlay}`.
  Batch on the robot side (~100 ms, max ~100 queued packets) so a fast
  OpMode cannot flood the socket.
- Web: an additive state slice in `useTuner` (not a new screen state —
  a new screen state would re-render/replace the screen per frame).
  Render in `components/screens/opMode.tsx` below the stop button.
- Depends on: the completed telemetry interface
  (`TelemetryFinder.inject()` wired into `TuningOpMode.runOpMode()`).

## 2. Config variables (tabs 1 + 2)

What it is: live-editable tuning constants, grouped in collapsible
categories; text/number/boolean/enum inputs with validation; save sends
only changed + valid values; a filter shows only values differing from
baseline.

- Robot: the interface is done —
  `Tuning/src/main/java/com/pedropathing/tuning/autotune/config/`
  (`Var`, `ConfigVar.of/required/ofNullable`, `Validation`, `Validator`,
  `Modifier`, `Variables` registry, `Section`, `Configuration` lambdas).
  What remains is the transport: a channel over the existing WebSocket
  (new message types, added — never changed) or a new HTTP endpoint for
  get/set, bridging the `Variables` registry to the web UI. Validation
  errors from `set()` must surface in the UI.
- Web: reuse the `Inputs.Field` model and `components/inputs/*` +
  zod `fieldSchema` for the editors (a config-variable system is
  "persistent `Inputs.Field`s with a get/set channel instead of a
  one-shot submit"). Global set in tab 2 (grouped by section/category);
  per-procedure vars in tab 1 where a procedure declares them.
- Dashboard reference: robot `@Config` reflection OR
  `addConfigVariable(category, name, ValueProvider)`; types boolean /
  int / long / float / double / string / readonly_string / enum / nested
  objects; server pushes the tree as `RECEIVE_CONFIG` on connect and on
  change; client edits locally, Save sends `SAVE_CONFIG` with the diff;
  `GET/RECEIVE_CONFIG_BASELINE` captures startup values for the modified
  filter. **In AutoTune this is the FTCUtils-Variables registry (§4 of
  AGENT_CONTEXT.md), not Dashboard's `@Config` reflection.**

## 3. OpMode list & control (tab 2)

What it is: dropdown of all OpModes grouped by group; state-aware
buttons — stopped → Init only; INIT → Start + Stop; RUNNING → Stop only.

- Robot: list built once from the registered OpModes (exclude SYSTEM
  flavor, sort by group then name) and pushed to the client on connect;
  init/start/stop dispatches through `Hooks.getOpModeManager()` (public
  since the fork's `6a0b1c2`). Reuse the existing `TuningSession` stop
  path (`stopOpMode` → `requestOpModeStop()` when `canStop`).
- Constraint: one active session (§2 of AGENT_CONTEXT.md). OpMode control
  is only available when no procedure is running, or it takes over the
  session explicitly — pick one and document it.
- Web: state machine in `useTuner` mirroring the OpMode lifecycle;
  buttons enable/disable by state.
- Depends on: telemetry (§1) for the status strip below.

## 4. Robot status + heartbeat (tab 2, status strip)

What it is: header strip — connection icon, ping (ms), battery voltage,
OpMode status INIT/RUNNING/STOPPED, warning/error strings.

- Robot: a 1 s heartbeat the client requests and the server answers with
  status (enabled, active OpMode + status, warning/error message, battery
  voltage). Model on Dashboard's `RECEIVE_ROBOT_STATUS`.
- Web: header strip above the tabs (part of Chrome, fed by the heartbeat).

## 5. Camera stream (tab 2) — see AGENT_CONTEXT.md §6

What it is: live camera frames in a rotatable canvas (0/90/180/270°).

- Robot: a new `Display` subtype + factory (`Display.camera(...)`),
  `registerSubtype` in `WebSocketServer`'s Gson factory, a zod variant in
  `displaySchema`, a new case in `display.tsx` + a renderer component.
  The hard part is the frame source: standard webcam frames come from
  VisionPortal as MJPEG or via a `sendImage`-style path (check the
  VisionPortal API in the robot code — no camera code exists in this
  repo); Limelight is proxied, not re-implemented (forward
  `172.29.0.1` → Hub Wi-Fi: 5800 MJPEG, 5801 web UI, 5805 WS, 5807 REST;
  client polls `:5807/status` every 2 s).
- One mode covers both sources (a source selector inside the camera
  Display, not two Display subtypes).

## 6. Field view (tab 2)

What it is: field image with a canvas overlay drawn from telemetry
(robot pose, paths, shapes, text, images, grids); custom
origins/rotation/scale.

- Robot: OpModes draw into a field-overlay canvas attached to the
  telemetry packet (Dashboard: `packet.fieldOverlay()`, a `Canvas`
  recording ops); ops travel inside the telemetry packet.
- Web: replay the ops over a field image. Dashboard's default field is
  `/dash/decode.webp` 144×144 in + 7×7 grid (season-specific — AutoTune
  needs its own field image).
- Depends on: telemetry (§1).

## 7. Diagnostics tab (new tab)

Three features belong in none of tabs 1–3 — they are retrospective, not
live control — so they get their own tab instead of cluttering tab 2.

- **Error / logcat view.** Scrolling list of robot errors (OpModeManager
  tag only), auto-scroll toggle, clear button. Robot: tail logcat and
  push errors over the socket (Dashboard: `RECEIVE_LOGCAT_ERRORS`).
- **Logging view (telemetry history + CSV export).** Timestamped table of
  all past telemetry values, per-key show/hide, CSV download. Pure web:
  accumulate the telemetry store into a local table; download exports
  CSV. Cheap once the telemetry feed exists.
- **Recorder / replay.** Record up to 10,000 telemetry samples (~2.5
  min), save named replays to localStorage (0.2 MB each, 5 MB total),
  replay field overlays onto the Field view at adjustable opacity.
  Pure web: ring buffer + `localStorage` keys `field_replay_*`. Cross-tab
  link: replay needs a "show on field" affordance into tab 2's field
  view.

## 8. Telemetry graph (tab 2)

What it is: plots selected numeric telemetry keys over a configurable
time window; play/pause; key picker.

- Pure web: reads the telemetry store, no new messages. Needs numeric
  values in telemetry packets.
- Depends on: telemetry (§1).

## 9. Hardware config editor (tab 3)

What it is: list, select, edit (text or GUI), save-as, and delete the
robot's hardware configuration XML files; shows which config is active
and read-only flags.

- Robot: read the `RobotConfigFileManager` XML files and push the list
  (name, xml content, readOnly, active config) to the client; accept
  set/write/delete operations from the client.
- Web: file list + editor (text first; GUI later if ever).

## 10. HardwareOpMode live hardware control (tab 3)

What it is: a built-in `@TeleOp(name="Hardware", group="Dashboard")`
that auto-discovers motors/servos in the hardware map and exposes live
power/position/current/port info for direct control and monitoring.

- Robot: an OpMode that enumerates the hardware map and surfaces values
  as config vars under the special `__hardware__` category (same editor
  as §2, hardware category).
- Web: the hardware view renders the `__hardware__` vars.

## 11. Gamepad input (tab 2) — defer, last or never

What it is: drive the robot from the browser — physical USB gamepads
(Browser Gamepad API), on-screen virtual sticks/buttons/sliders, or
keyboard mapping.

- Web: poll `navigator.getGamepads()` every animation frame, 0.2
  deadzone + SDK-style rescaling, send gamepad state; 500 ms watchdog
  zeroes the inputs if updates stop.
- Robot: copy the received state into SDK `Gamepad` objects.
- Dashboard's own docs warn: higher latency, less robust than a real
  driver station — use only for testing. Highest risk, lowest value for
  a tuner — implement last or not at all.

## 12. Chrome (global, not a tab)

- **Dashboard enable/disable + auto-enable.** Master on/off for the
  whole dashboard (in-app OpMode + RC menu item); auto-enable remembered
  across restarts. In AutoTune this is a global concern (server
  start/stop), not a tab.
- **Layout presets + custom layout.** In a tabbed UI the tab system
  itself is the layout — plus an optional per-tab preset switcher. Do
  not invent a tab for it.
- **Settings (theme) + header status.** Settings modal (accent color,
  dark/light mode); header shows connection icon, ping, battery voltage
  (battery value is robot-side data, from the §4 heartbeat).

## Open questions (from AGENT_CONTEXT.md §11, relevant here)

- Whether the OpMode list is re-pushed to late-joining clients.
- `SAVE_CONFIG` diff handling for nested objects/arrays (flat
  primitives/enums are confirmed; nested behavior was not verified).
