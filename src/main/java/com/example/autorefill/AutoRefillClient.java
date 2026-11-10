package com.example.autorefill;

import com.example.autorefill.AutoRefillConfig.Rule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class AutoRefillClient implements ClientModInitializer {
    private KeyBinding menuKey;
    private int cooldown = 0;

    @Override
    public void onInitializeClient() {
        AutoRefillConfig.load();
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "Mở menu Auto Refill", InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT, "Auto Refill"));
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    private void tick(MinecraftClient mc) {
        while (menuKey.wasPressed()) {
            if (mc.currentScreen == null) mc.setScreen(new AutoRefillScreen());
        }

        ClientPlayerEntity player = mc.player;
        if (player == null || mc.interactionManager == null || mc.currentScreen != null) return;
        if (cooldown > 0) { cooldown--; return; }

        for (Rule r : AutoRefillConfig.rules) {
            int hb = r.slot - 1;
            if (hb < 0 || hb > 8) continue;
            if (!player.getInventory().getStack(hb).isEmpty()) continue;

            Identifier id = Identifier.tryParse(r.item);
            if (id == null || !Registries.ITEM.containsId(id)) continue;
            var item = Registries.ITEM.get(id);

            for (int i = 9; i < 36; i++) {
                if (player.getInventory().getStack(i).isOf(item)) {
                    mc.interactionManager.clickSlot(
                        player.playerScreenHandler.syncId, i, hb,
                        SlotActionType.SWAP, player);
                    cooldown = 5;
                    return;
                }
            }
        }
    }
}
