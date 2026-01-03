package com.customcrystals.screen;

import com.customcrystals.config.CrystalConfig;
import com.customcrystals.config.CrystalConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CrystalConfigScreen extends Screen {
    private static final int CONTROL_WIDTH = 220;
    private static final int CONTROL_HEIGHT = 20;
    private final Screen parent;
    private CrystalConfig working;
    private EditBox coreColorField;
    private EditBox frame1ColorField;
    private EditBox frame2ColorField;

    public CrystalConfigScreen(Screen parent) {
        super(Component.translatable("screen.customcrystals.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.working = copyConfig(CrystalConfigManager.get());
        int centerX = this.width / 2;
        int y = 60;

        addRenderableWidget(new FloatSlider(centerX - CONTROL_WIDTH / 2, y, CONTROL_WIDTH, CONTROL_HEIGHT,
                0.25f, 3.0f, working.scale,
                value -> Component.translatable("option.customcrystals.scale", String.format("%.2fx", value)),
                value -> working.scale = (float) value));

        y += 26;
        addRenderableWidget(new FloatSlider(centerX - CONTROL_WIDTH / 2, y, CONTROL_WIDTH, CONTROL_HEIGHT,
                -1.5f, 1.5f, working.verticalOffset,
                value -> Component.translatable("option.customcrystals.vertical_offset", String.format("%.2f", value)),
                value -> working.verticalOffset = (float) value));

        y += 26;
        addRenderableWidget(new FloatSlider(centerX - CONTROL_WIDTH / 2, y, CONTROL_WIDTH, CONTROL_HEIGHT,
                0.25f, 3.0f, working.spinMultiplier,
                value -> Component.translatable("option.customcrystals.spin", String.format("%.2fx", value)),
                value -> working.spinMultiplier = (float) value));

        y += 26;
        addRenderableWidget(CycleButton.onOffBuilder()
            .withInitialValue(working.coreTintEnabled)
                .create(centerX - CONTROL_WIDTH / 2, y, CONTROL_WIDTH, CONTROL_HEIGHT,
                Component.translatable("option.customcrystals.core_tint"),
                (button, value) -> working.coreTintEnabled = value));

        y += 26;
        addRenderableWidget(CycleButton.onOffBuilder()
                .withInitialValue(working.beamEnabled)
                .create(centerX - CONTROL_WIDTH / 2, y, CONTROL_WIDTH, CONTROL_HEIGHT,
                        Component.translatable("option.customcrystals.beam"),
                        (button, value) -> working.beamEnabled = value));

        y += 30;
        coreColorField = new EditBox(this.font, centerX - CONTROL_WIDTH / 2, y, CONTROL_WIDTH, CONTROL_HEIGHT, Component.translatable("option.customcrystals.core_color"));
        coreColorField.setMaxLength(6);
        coreColorField.setValue(String.format("%06X", working.coreColor));
        addRenderableWidget(coreColorField);

        y += 26;
        addRenderableWidget(CycleButton.onOffBuilder()
            .withInitialValue(working.frame1TintEnabled)
            .create(centerX - CONTROL_WIDTH / 2, y, CONTROL_WIDTH, CONTROL_HEIGHT,
                Component.translatable("option.customcrystals.frame1_tint"),
                (button, value) -> working.frame1TintEnabled = value));

        y += 26;
        frame1ColorField = new EditBox(this.font, centerX - CONTROL_WIDTH / 2, y, CONTROL_WIDTH, CONTROL_HEIGHT, Component.translatable("option.customcrystals.frame1_color"));
        frame1ColorField.setMaxLength(6);
        frame1ColorField.setValue(String.format("%06X", working.frame1Color));
        addRenderableWidget(frame1ColorField);

        y += 26;
        addRenderableWidget(CycleButton.onOffBuilder()
            .withInitialValue(working.frame2TintEnabled)
            .create(centerX - CONTROL_WIDTH / 2, y, CONTROL_WIDTH, CONTROL_HEIGHT,
                Component.translatable("option.customcrystals.frame2_tint"),
                (button, value) -> working.frame2TintEnabled = value));

        y += 26;
        frame2ColorField = new EditBox(this.font, centerX - CONTROL_WIDTH / 2, y, CONTROL_WIDTH, CONTROL_HEIGHT, Component.translatable("option.customcrystals.frame2_color"));
        frame2ColorField.setMaxLength(6);
        frame2ColorField.setValue(String.format("%06X", working.frame2Color));
        addRenderableWidget(frame2ColorField);

        int buttonY = this.height - 30;
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> close())
                .bounds(centerX - CONTROL_WIDTH / 2, buttonY, 100, CONTROL_HEIGHT)
                .build());

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> {
                    applyEdits();
                    CrystalConfigManager.update(working);
                    close();
                })
                .bounds(centerX + CONTROL_WIDTH / 2 - 100, buttonY, 100, CONTROL_HEIGHT)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.fillGradient(0, 0, this.width, this.height, 0xB0000000, 0xE0000000);
        super.render(graphics, mouseX, mouseY, delta);
        int centerX = this.width / 2;
        int labelX = centerX - CONTROL_WIDTH / 2;
        graphics.drawString(this.font, Component.translatable("option.customcrystals.core_color_label"),
            labelX, coreColorField.getY() - 10, 0xFFFFFF, false);
        graphics.drawString(this.font, Component.translatable("option.customcrystals.frame1_color_label"),
            labelX, frame1ColorField.getY() - 10, 0xFFFFFF, false);
        graphics.drawString(this.font, Component.translatable("option.customcrystals.frame2_color_label"),
            labelX, frame2ColorField.getY() - 10, 0xFFFFFF, false);
    }

    @Override
    public void onClose() {
        close();
    }

    private void applyEdits() {
        working.coreColor = parseColor(coreColorField.getValue());
        working.frame1Color = parseColor(frame1ColorField.getValue());
        working.frame2Color = parseColor(frame2ColorField.getValue());
    }

    private void close() {
        Minecraft client = this.minecraft;
        if (client != null) {
            client.setScreen(parent);
        }
    }

    private int parseColor(String input) {
        String clean = input.trim().replace("#", "");
        if (clean.length() > 6) {
            clean = clean.substring(clean.length() - 6);
        }
        try {
            return Integer.parseInt(clean, 16) & 0xFFFFFF;
        } catch (NumberFormatException e) {
            return CrystalConfig.defaults().coreColor;
        }
    }

    private static CrystalConfig copyConfig(CrystalConfig base) {
        CrystalConfig copy = new CrystalConfig();
        copy.coreTintEnabled = base.coreTintEnabled;
        copy.coreColor = base.coreColor;
        copy.frame1TintEnabled = base.frame1TintEnabled;
        copy.frame1Color = base.frame1Color;
        copy.frame2TintEnabled = base.frame2TintEnabled;
        copy.frame2Color = base.frame2Color;
        copy.scale = base.scale;
        copy.verticalOffset = base.verticalOffset;
        copy.beamEnabled = base.beamEnabled;
        copy.spinMultiplier = base.spinMultiplier;
        return copy;
    }
}
