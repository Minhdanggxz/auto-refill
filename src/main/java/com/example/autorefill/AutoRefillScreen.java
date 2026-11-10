package com.example.autorefill;

import com.example.autorefill.AutoRefillConfig.Rule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;

public class AutoRefillScreen extends Screen {
    private final List<TextFieldWidget> itemFields = new ArrayList<>();
    private final List<TextFieldWidget> slotFields = new ArrayList<>();

    public AutoRefillScreen() {
        super(Text.literal("Auto Refill Hotbar"));
    }

    private static boolean validItem(String s) {
        Identifier id = Identifier.tryParse(s.trim());
        return id != null && Registries.ITEM.containsId(id);
    }

    @Override
    protected void init() {
        itemFields.clear();
        slotFields.clear();
        List<Rule> rules = AutoRefillConfig.rules;
        int x = width / 2 - 115;
        int y = 35;

        for (int i = 0; i < rules.size(); i++) {
            Rule r = rules.get(i);

            TextFieldWidget item = new TextFieldWidget(textRenderer, x, y, 170, 20, Text.empty());
            item.setMaxLength(100);
            item.setText(r.item);
            item.setEditableColor(validItem(r.item) ? 0xE0E0E0 : 0xFF5555);
            item.setChangedListener(s -> item.setEditableColor(validItem(s) ? 0xE0E0E0 : 0xFF5555));

            TextFieldWidget slot = new TextFieldWidget(textRenderer, x + 175, y, 30, 20, Text.empty());
            slot.setMaxLength(1);
            slot.setText(String.valueOf(r.slot));

            final int idx = i;
            addDrawableChild(item);
            addDrawableChild(slot);
            addDrawableChild(ButtonWidget.builder(Text.literal("X"), b -> {
                sync();
                rules.remove(idx);
                clearAndInit();
            }).dimensions(x + 210, y, 20, 20).build());

            itemFields.add(item);
            slotFields.add(slot);
            y += 24;
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("+ Thêm"), b -> {
            sync();
            rules.add(new Rule("minecraft:", 1));
            clearAndInit();
        }).dimensions(x, y + 6, 110, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Lưu & đóng"), b -> close())
            .dimensions(x + 120, y + 6, 110, 20).build());
    }

    private void sync() {
        List<Rule> rules = AutoRefillConfig.rules;
        for (int i = 0; i < itemFields.size() && i < rules.size(); i++) {
            Rule r = rules.get(i);
            r.item = itemFields.get(i).getText().trim();
            try {
                r.slot = Math.max(1, Math.min(9, Integer.parseInt(slotFields.get(i).getText().trim())));
            } catch (NumberFormatException ignored) {
                r.slot = 1;
            }
        }
    }

    @Override
    public void close() {
        sync();
        AutoRefillConfig.save();
        super.close();
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFF);
        ctx.drawTextWithShadow(textRenderer, "Item ID", width / 2 - 115, 24, 0xAAAAAA);
        ctx.drawTextWithShadow(textRenderer, "Ô", width / 2 + 62, 24, 0xAAAAAA);
    }
}
