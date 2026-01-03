package com.customcrystals.screen;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class FloatSlider extends AbstractSliderButton {
    private final double min;
    private final double max;
    private final ValueFormatter formatter;
    private final ValueChanged onChange;

    public interface ValueFormatter {
        Component format(double value);
    }

    public interface ValueChanged {
        void onChange(double value);
    }

    public FloatSlider(int x, int y, int width, int height, double min, double max, double current, ValueFormatter formatter, ValueChanged onChange) {
        super(x, y, width, height, Component.empty(), toProgress(current, min, max));
        this.min = min;
        this.max = max;
        this.formatter = formatter;
        this.onChange = onChange;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(formatter.format(valueToConfig(value)));
    }

    @Override
    protected void applyValue() {
        onChange.onChange(valueToConfig(value));
    }

    private double valueToConfig(double sliderValue) {
        double clamped = Mth.clamp(sliderValue, 0.0d, 1.0d);
        return clamped * (max - min) + min;
    }

    private static double toProgress(double current, double min, double max) {
        if (max - min == 0) return 0.0d;
        return Mth.clamp((current - min) / (max - min), 0.0d, 1.0d);
    }
}
