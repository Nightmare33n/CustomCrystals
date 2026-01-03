package com.customcrystals;

import com.customcrystals.config.CrystalConfigManager;
import com.customcrystals.screen.CrystalConfigScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;
public class CustomCrystalsClient implements ClientModInitializer {
    private static KeyMapping openConfigKey;

    @Override
    public void onInitializeClient() {
        CrystalConfigManager.load();

        Category category = Category.register(ResourceLocation.fromNamespaceAndPath(CustomCrystals.MOD_ID, "controls"));
        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.customcrystals.open_config",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openConfigKey != null && openConfigKey.consumeClick()) {
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.setScreen(new CrystalConfigScreen(minecraft.screen));
            }
        });
    }
}
