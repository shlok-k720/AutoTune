package com.pedropathing.tuning.autotune;

import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.internal.opmode.TelemetryImpl;

import java.util.ArrayList;

public class TelemetryFinder {
    private static TelemetryFinder instance;

    public static TelemetryFinder getInstance() {
        if (instance == null) {
            instance = new TelemetryFinder();
        }
        return instance;
    }

    private LogTelemetry telemetry;

    private TelemetryFinder() {

    }

    public TelemetryImpl findTelemetry() {
        return (TelemetryImpl) Hooks.getOpModeManager().getActiveOpMode().telemetry;
    }

    public void inject() {
        Hooks.getOpModeManager().getActiveOpMode().telemetry = buildLogTelemetry();
    }

    private LogTelemetry buildLogTelemetry() {
        telemetry = new LogTelemetry(findTelemetry());
        return telemetry;
    }

    public Telemetry.Item[] telemetryData() {
        requireTelemetry();
        return null;
    }

    private void requireTelemetry() {
        if (!(findTelemetry() instanceof LogTelemetry)) {
            throw new IllegalStateException("Telemetry not injected, cannot run telemetry operations");
        }
        if (findTelemetry() != telemetry && findTelemetry() instanceof LogTelemetry) {
            telemetry = (LogTelemetry) findTelemetry();
        }
    }


}

class LogTelemetry extends TelemetryImpl {// TODO: Telemetry Impl
    TelemetryImpl source;
    ArrayList<Telemetry.Item> items = new ArrayList<>();
    public LogTelemetry(TelemetryImpl source) {
        super(Hooks.getOpModeManager().getActiveOpMode());
        this.source = source;
    }

    private Item addItem(Item item) {
        items.add(item);
        return item;
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
        return source.removeItem(item);
    }

    @Override
    public void clear() {
        items.clear(); // TODO - figure out exact logic
        source.clear();
    }

    @Override
    public void clearAll() {
        items.clear();
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
        return source.update();
    }

    @Override
    public Line addLine() {
        return source.addLine();
    }

    @Override
    public Line addLine(String lineCaption) {
        return source.addLine(lineCaption);
    }

    @Override
    public boolean removeLine(Line line) {
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