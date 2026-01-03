package com.customcrystals.screen;

import com.customcrystals.config.CrystalConfig;
import com.customcrystals.config.CrystalConfigManager;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.CheckboxComponent;
import io.wispforest.owo.ui.component.ColorPickerComponent;
import io.wispforest.owo.ui.component.DiscreteSliderComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

public class CrystalConfigScreen extends BaseOwoScreen<FlowLayout> {
    private final Screen parent;
    private CrystalConfig working;

    public CrystalConfigScreen(Screen parent) {
        super(Component.translatable("screen.customcrystals.title"));
        this.parent = parent;
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        this.working = copyConfig(CrystalConfigManager.get());

        FlowLayout content = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        content.gap(10);
        content.verticalAlignment(VerticalAlignment.TOP);
        content.horizontalAlignment(HorizontalAlignment.CENTER);

        LabelComponent titleLabel = io.wispforest.owo.ui.component.Components.label(this.title.copy()).color(io.wispforest.owo.ui.core.Color.WHITE).shadow(true);
        titleLabel.horizontalSizing(Sizing.fill(100));
        titleLabel.horizontalTextAlignment(HorizontalAlignment.CENTER);
        content.child(titleLabel);

        FlowLayout sliders = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        sliders.gap(10);
        sliders.child(sliderCard("option.customcrystals.scale", 0.25f, 3.0f, working.scale, v -> working.scale = v, v -> String.format("%.2fx", v)));
        sliders.child(sliderCard("option.customcrystals.vertical_offset", -1.5f, 1.5f, working.verticalOffset, v -> working.verticalOffset = v, v -> String.format("%.2f", v)));
        sliders.child(sliderCard("option.customcrystals.spin", 0.25f, 3.0f, working.spinMultiplier, v -> working.spinMultiplier = v, v -> String.format("%.2fx", v)));
        sliders.child(toggleRow("option.customcrystals.beam", working.beamEnabled, value -> working.beamEnabled = value));
        content.child(sliders);

        FlowLayout colorCards = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        colorCards.gap(8);
        colorCards.child(colorCard("option.customcrystals.core_color", "option.customcrystals.core_tint", working.coreTintEnabled, value -> working.coreTintEnabled = value, working.coreColor, value -> working.coreColor = value));
        colorCards.child(colorCard("option.customcrystals.frames_color", "option.customcrystals.frames_tint", working.framesTintEnabled, value -> working.framesTintEnabled = value, working.framesColor, value -> working.framesColor = value));
        content.child(colorCards);

        FlowLayout buttons = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        buttons.gap(8);

        ButtonComponent cancel = io.wispforest.owo.ui.component.Components.button(Component.translatable("gui.cancel"), button -> closeToParent());
        cancel.sizing(Sizing.fill(33), Sizing.fixed(20));

        ButtonComponent reset = io.wispforest.owo.ui.component.Components.button(Component.translatable("controls.reset"), button -> {
            CrystalConfig defaults = CrystalConfig.defaults();
            CrystalConfigManager.update(defaults);
            Minecraft client = this.minecraft;
            if (client != null) {
                client.setScreen(new CrystalConfigScreen(parent));
            }
        });
        reset.sizing(Sizing.fill(33), Sizing.fixed(20));

        ButtonComponent save = io.wispforest.owo.ui.component.Components.button(Component.translatable("gui.done"), button -> {
            CrystalConfigManager.update(working);
            closeToParent();
        });
        save.sizing(Sizing.fill(33), Sizing.fixed(20));

        buttons.child(cancel);
        buttons.child(reset);
        buttons.child(save);
        content.child(buttons);

        ScrollContainer<FlowLayout> scroller = Containers.verticalScroll(Sizing.fill(100), Sizing.fill(100), content);
        scroller.surface(Surface.VANILLA_TRANSLUCENT);
        scroller.padding(Insets.of(16));
        scroller.scrollbar(ScrollContainer.Scrollbar.vanilla());

        root.child(scroller);
    }

    private FlowLayout sliderCard(String key, float min, float max, float currentValue, Consumer<Float> onChange, Function<Float, String> formatter) {
        FlowLayout card = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        card.surface(Surface.flat(0xAA111111));
        card.padding(Insets.of(10));
        card.gap(6);

        LabelComponent label = io.wispforest.owo.ui.component.Components.label(Component.translatable(key, formatter.apply(currentValue))).color(io.wispforest.owo.ui.core.Color.WHITE).shadow(true);
        card.child(label);

        int minInt = Math.round(min * 100);
        int maxInt = Math.round(max * 100);

        DiscreteSliderComponent slider = io.wispforest.owo.ui.component.Components.discreteSlider(Sizing.fill(100), minInt, maxInt)
                .decimalPlaces(0)
                .snap(true)
                .setFromDiscreteValue(Math.round(currentValue * 100));
        slider.onChanged().subscribe(val -> {
            float value = ((float) val) / 100f;
            onChange.accept(value);
            label.text(Component.translatable(key, formatter.apply(value)));
        });
        card.child(slider);
        return card;
    }

    private FlowLayout toggleRow(String key, boolean initial, Consumer<Boolean> onToggle) {
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        row.gap(8);
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.surface(Surface.flat(0xAA111111));
        row.padding(Insets.of(8));

        CheckboxComponent checkbox = io.wispforest.owo.ui.component.Components.checkbox(Component.translatable(key));
        checkbox.checked(initial);
        checkbox.onChanged(onToggle::accept);
        row.child(checkbox);

        return row;
    }

    private FlowLayout colorCard(String titleKey, String toggleKey, boolean tintEnabled, Consumer<Boolean> onToggle, int color, Consumer<Integer> onColorChange) {
        FlowLayout card = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        card.surface(Surface.flat(0xAA111111));
        card.padding(Insets.of(10));
        card.gap(8);

        FlowLayout header = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        header.verticalAlignment(VerticalAlignment.CENTER);
        header.gap(8);

        CheckboxComponent toggle = io.wispforest.owo.ui.component.Components.checkbox(Component.translatable(toggleKey));
        toggle.checked(tintEnabled);
        toggle.onChanged(onToggle::accept);

        LabelComponent label = io.wispforest.owo.ui.component.Components.label(Component.translatable(titleKey)).color(io.wispforest.owo.ui.core.Color.WHITE).shadow(true);
        header.child(label);
        header.child(toggle);

        ColorPickerComponent picker = new ColorPickerComponent();
        picker.selectedColor(io.wispforest.owo.ui.core.Color.ofRgb(color));
        picker.showAlpha(false);
        picker.sizing(Sizing.fill(100), Sizing.fixed(140));
        picker.onChanged().subscribe(next -> onColorChange.accept(next.rgb()));

        card.child(header);
        card.child(picker);
        return card;
    }

    private void closeToParent() {
        Minecraft client = this.minecraft;
        if (client != null) {
            client.setScreen(parent);
        }
    }

    @Override
    public void onClose() {
        closeToParent();
    }

    private static CrystalConfig copyConfig(CrystalConfig base) {
        CrystalConfig copy = new CrystalConfig();
        copy.coreTintEnabled = base.coreTintEnabled;
        copy.coreColor = base.coreColor;
        copy.framesTintEnabled = base.framesTintEnabled;
        copy.framesColor = base.framesColor;
        copy.scale = base.scale;
        copy.verticalOffset = base.verticalOffset;
        copy.beamEnabled = base.beamEnabled;
        copy.spinMultiplier = base.spinMultiplier;
        return copy;
    }
}
