package com.example.autorefill;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.*;
import java.util.*;

public class AutoRefillConfig {
    public static class Rule {
        public String item;
        public int slot; // 1-9
        public Rule(String item, int slot) { this.item = item; this.slot = slot; }
    }

    public static final List<Rule> rules = new ArrayList<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("autorefill.json");
    }

    public static void load() {
        try {
            if (!Files.exists(path())) {
                rules.add(new Rule("minecraft:bone_meal", 2));
                save();
                return;
            }
            List<Rule> l = GSON.fromJson(Files.readString(path()),
                new TypeToken<List<Rule>>() {}.getType());
            rules.clear();
            if (l != null) rules.addAll(l);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void save() {
        try { Files.writeString(path(), GSON.toJson(rules)); }
        catch (Exception e) { e.printStackTrace(); }
    }
}
