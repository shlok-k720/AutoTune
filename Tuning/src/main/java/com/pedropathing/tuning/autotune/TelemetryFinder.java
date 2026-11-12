package com.pedropathing.tuning.autotune;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl;

import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.internal.opmode.TelemetryImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Finds the active OpMode's telemetry and optionally swaps it for a tracking
 * wrapper ({@link LogTelemetry}) so the web tuner can read back what an
 * OpMode added to telemetry.
 *
 * <p>Tracking model: the wrapper records every {@link Telemetry.Item}
 * created through its top-level {@code addData} calls and every
 * {@link Telemetry.Line} created through its top-level {@code addLine}
 * calls, and exposes them via {@link #telemetryData()}, {@link #getItems()},
 * {@link #telemetryLines()} and {@link #getLines()}. The tracked lists
 * mirror the wrapped telemetry: {@code clear()} and {@code update()} (when
 * auto-clear is on) drop non-retained entries, {@code clearAll()} drops
 * everything, and {@code removeItem} / {@code removeLine} drop the entry
 * from the lists as well.</p>
 *
 * <p>Known limits -- all follow from the public {@link Telemetry} API:</p>
 * <ul>
 *   <li>Items added through a {@code Line.addData(...)} call are not
 *   tracked: {@code Line.addData} returns the line, not the item, so the
 *   wrapper is never handed the created item. Likewise, items and lines
 *   added through an {@code Item.addData(...)} / {@code Item.addLine(...)}
 *   call are not tracked. OpModes that want their data visible to the
 *   tuner should use top-level {@code telemetry.addData(...)}.</li>
 *   <li>A tracked {@code Line}'s contents cannot be read back through the
 *   public API either (there is no accessor for a line's items), so lines
 *   are exposed as opaque rows. Before rendering line contents, the
 *   transport step should check whether the concrete
 *   {@code TelemetryImpl.Line} exposes its items.</li>
 * </ul>
 *
 * <p>Threading: {@link #getInstance()} uses the initialization-on-demand
 * holder idiom. The tracked lists are {@link CopyOnWriteArrayList}s so a
 * transport thread can snapshot them while the OpMode thread is adding
 * items; {@link #getItems()} / {@link #telemetryData()} return defensive
 * snapshots.</p>
 *
 * <p>Depends on the SDK-internal
 * {@code org.firstinspires.ftc.robotcore.internal.opmode.TelemetryImpl} --
 * verify this still compiles and runs against FTC SDK 12.0.0 before relying
 * on it; if the internal class changes, fall back to a plain
 * {@link Telemetry} interface wrapper.</p>
 */
public class TelemetryFinder {

    private static class Holder {
        static final TelemetryFinder INSTANCE = new TelemetryFinder();
    }

    /** Initialization-on-demand holder idiom: thread-safe with no synchronization cost. */
    public static TelemetryFinder getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * The wrapper currently installed on the active OpMode, if any.
     * Volatile so a transport thread sees installs and restores.
     */
    private volatile LogTelemetry telemetry;

    private TelemetryFinder() {

    }

    /**
     * Returns the active OpMode's telemetry as a {@link TelemetryImpl}, or
     * null when there is no active OpMode or its telemetry is not a
     * {@code TelemetryImpl}. Never throws.
     */
    public TelemetryImpl findTelemetry() {
        OpModeManagerImpl manager = Hooks.getOpModeManager();
        if (manager == null) return null;
        OpMode active = manager.getActiveOpMode();
        if (active == null) return null;
        Telemetry activeTelemetry = active.telemetry;
        return activeTelemetry instanceof TelemetryImpl ? (TelemetryImpl) activeTelemetry : null;
    }

    /**
     * Swaps the active OpMode's telemetry for the tracking wrapper.
     * Safe to call any time: does nothing when there is no active OpMode,
     * when its telemetry is not a {@link TelemetryImpl}, or when the wrapper
     * is already installed (in which case the installed wrapper is adopted).
     * The swap only happens if the active OpMode's telemetry is still the
     * instance that was wrapped, so a concurrent OpMode switch cannot cause
     * the wrapper to land on the wrong OpMode. Never throws.
     */
    public void inject() {
        TelemetryImpl current = findTelemetry();
        if (current == null) return;
        if (current instanceof LogTelemetry) {
            telemetry = (LogTelemetry) current;
            return;
        }
        OpModeManagerImpl manager = Hooks.getOpModeManager();
        OpMode active = manager != null ? manager.getActiveOpMode() : null;
        if (active == null || active.telemetry != current) return;
        LogTelemetry wrapper = new LogTelemetry(current, active);
        telemetry = wrapper;
        active.telemetry = wrapper;
    }

    /**
     * Restores the original telemetry on the active OpMode, undoing
     * {@link #inject()}. Safe to call any time: does nothing when nothing
     * was injected, when the active OpMode is gone, or when the active
     * OpMode's telemetry is no longer the installed wrapper (it is left
     * alone rather than clobbered). Never throws.
     */
    public void restore() {
        LogTelemetry installed = telemetry;
        if (installed == null) return;
        OpModeManagerImpl manager = Hooks.getOpModeManager();
        OpMode active = manager != null ? manager.getActiveOpMode() : null;
        if (active == null) {
            telemetry = null;
            return;
        }
        if (active.telemetry == installed) {
            active.telemetry = installed.source;
        }
        telemetry = null;
    }

    /** True when the tracking wrapper is currently installed on the active OpMode's telemetry. */
    public boolean isInjected() {
        return findTelemetry() instanceof LogTelemetry;
    }

    /**
     * Returns a snapshot of the items tracked since injection, as an array.
     * The returned array is a copy: later telemetry calls do not change it.
     *
     * @throws IllegalStateException when the wrapper is not injected
     */
    public Telemetry.Item[] telemetryData() {
        return requireTelemetry().items.toArray(new Telemetry.Item[0]);
    }

    /**
     * Returns a defensive copy of the tracked items.
     *
     * @throws IllegalStateException when the wrapper is not injected
     */
    public List<Telemetry.Item> getItems() {
        return new ArrayList<>(requireTelemetry().items);
    }

    /**
     * Returns a snapshot of the lines tracked since injection, as an array.
     * The returned array is a copy: later telemetry calls do not change it.
     *
     * @throws IllegalStateException when the wrapper is not injected
     */
    public Telemetry.Line[] telemetryLines() {
        return requireTelemetry().lines.toArray(new Telemetry.Line[0]);
    }

    /**
     * Returns a defensive copy of the tracked lines.
     *
     * @throws IllegalStateException when the wrapper is not injected
     */
    public List<Telemetry.Line> getLines() {
        return new ArrayList<>(requireTelemetry().lines);
    }

    private LogTelemetry requireTelemetry() {
        TelemetryImpl current = findTelemetry();
        if (!(current instanceof LogTelemetry)) {
            throw new IllegalStateException("Telemetry not injected, cannot run telemetry operations");
        }
        if (current != telemetry) {
            telemetry = (LogTelemetry) current;
        }
        return (LogTelemetry) current;
    }
}

/**
 * Tracking wrapper around a {@link TelemetryImpl}. Every {@code addData}
 * item and {@code addLine} line created through it is recorded, and the
 * recorded lists are kept in sync with the wrapped telemetry: removals,
 * clears and auto-clear after {@code update()} are mirrored. All other
 * calls are pure delegates to the wrapped instance, so Driver Station
 * behavior is unchanged.
 */
class LogTelemetry extends TelemetryImpl {
    final TelemetryImpl source;
    final List<Telemetry.Item> items = new CopyOnWriteArrayList<>();
    final List<Telemetry.Line> lines = new CopyOnWriteArrayList<>();

    LogTelemetry(TelemetryImpl source, OpMode opMode) {
        super(opMode);
        this.source = source;
    }

    private Item addItem(Item item) {
        items.add(item);
        return item;
    }

    private Line addLineInternal(Line line) {
        lines.add(line);
        return line;
    }

    /**
     * Drops non-retained entries from the tracked lists. This mirrors what
     * the wrapped telemetry does on {@code clear()} and on auto-clear after
     * {@code update()}: retained entries survive a clear, which is the
     * whole point of {@code setRetained(true)}.
     */
    private void dropNonRetained() {
        items.removeIf(item -> !item.isRetained());
        lines.removeIf(line -> !line.isRetained());
    }

    @Override
    public Item addData(String caption, String format, Object... args) {
        return addItem(source.addData(caption, format, args));
    }

    @Override
    public Item addData(String caption, Object value) {
        return addItem(source.addData(caption, value));
    }

    @Override
    public <T> Item addData(String caption, Func<T> valueProducer) {
        return addItem(source.addData(caption, valueProducer));
    }

    @Override
    public <T> Item addData(String caption, String format, Func<T> valueProducer) {
        return addItem(source.addData(caption, format, valueProducer));
    }

    @Override
    public boolean removeItem(Item item) {
        items.remove(item);
        return source.removeItem(item);
    }

    @Override
    public void clear() {
        dropNonRetained();
        source.clear();
    }

    @Override
    public void clearAll() {
        items.clear();
        lines.clear();
        source.clearAll();
    }

    @Override
    public Object addAction(Runnable action) {
        return source.addAction(action);
    }

    @Override
    public boolean removeAction(Object token) {
        return source.removeAction(token);
    }

    @Override
    public void speak(String text) {
        source.speak(text);
    }

    @Override
    public void speak(String text, String languageCode, String countryCode) {
        source.speak(text, languageCode, countryCode);
    }

    @Override
    public boolean update() {
        boolean transmitted = source.update();
        // TelemetryImpl clears non-retained entries after an update() when
        // auto-clear is on; mirror that so the tracked lists stay in sync
        // with the source. Assumption about the SDK's update() behavior --
        // verify against SDK 12.0.0 sources (see the context doc section 5).
        if (isAutoClear()) {
            dropNonRetained();
        }
        return transmitted;
    }

    @Override
    public Line addLine() {
        return addLineInternal(source.addLine());
    }

    @Override
    public Line addLine(String lineCaption) {
        return addLineInternal(source.addLine(lineCaption));
    }

    @Override
    public boolean removeLine(Line line) {
        lines.remove(line);
        return source.removeLine(line);
    }

    @Override
    public boolean isAutoClear() {
        return source.isAutoClear();
    }

    @Override
    public void setAutoClear(boolean autoClear) {
        source.setAutoClear(autoClear);
    }

    @Override
    public int getMsTransmissionInterval() {
        return source.getMsTransmissionInterval();
    }

    @Override
    public void setMsTransmissionInterval(int msTransmissionInterval) {
        source.setMsTransmissionInterval(msTransmissionInterval);
    }

    @Override
    public String getItemSeparator() {
        return source.getItemSeparator();
    }

    @Override
    public void setItemSeparator(String itemSeparator) {
        source.setItemSeparator(itemSeparator);
    }

    @Override
    public String getCaptionValueSeparator() {
        return source.getCaptionValueSeparator();
    }

    @Override
    public void setCaptionValueSeparator(String captionValueSeparator) {
        source.setCaptionValueSeparator(captionValueSeparator);
    }

    @Override
    public void setDisplayFormat(DisplayFormat displayFormat) {
        source.setDisplayFormat(displayFormat);
    }

    @Override
    public Log log() {
        return source.log();
    }
}
