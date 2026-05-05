package com.my.kaisen.registry;
 
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
 
import java.util.HashMap;
import java.util.Map;
 
public class VfxRegistry extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Map<String, VfxData> VFX_DATA_MAP = new HashMap<>();
 
    public VfxRegistry() {
        super(GSON, "vfx");
    }
 
    @Override
    protected void apply(Map<ResourceLocation, com.google.gson.JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
        VFX_DATA_MAP.clear();
        for (Map.Entry<ResourceLocation, com.google.gson.JsonElement> entry : objectIn.entrySet()) {
            if (entry.getValue().isJsonObject()) {
                String key = entry.getKey().getPath();
                VFX_DATA_MAP.put(key, new VfxData(entry.getValue().getAsJsonObject()));
            }
        }
    }
 
    public static VfxData get(String key) {
        return VFX_DATA_MAP.getOrDefault(key, VfxData.DEFAULT);
    }
 
    public static class VfxData {
        public static final VfxData DEFAULT = new VfxData();
        
        public final int startColor;
        public final int endColor;
        public final float scale;
        public final float spinSpeed;
        public final int lifespan;
        public final JsonObject raw;
 
        public VfxData() {
            this.startColor = 0xFFFFFF;
            this.endColor = 0xFFFFFF;
            this.scale = 1.0f;
            this.spinSpeed = 0.0f;
            this.lifespan = 20;
            this.raw = new JsonObject();
        }
 
        public VfxData(JsonObject json) {
            this.startColor = json.has("startColor") ? Integer.decode(json.get("startColor").getAsString()) : 0xFFFFFF;
            this.endColor = json.has("endColor") ? Integer.decode(json.get("endColor").getAsString()) : 0xFFFFFF;
            this.scale = json.has("scale") ? json.get("scale").getAsFloat() : 1.0f;
            this.spinSpeed = json.has("spinSpeed") ? json.get("spinSpeed").getAsFloat() : 0.0f;
            this.lifespan = json.has("lifespan") ? json.get("lifespan").getAsInt() : 20;
            this.raw = json;
        }
 
        public JsonObject getSubPhase(String phase) {
            return raw.has(phase) ? raw.getAsJsonObject(phase) : null;
        }
        
        public static VfxData fromJson(JsonObject json) {
            return new VfxData(json);
        }
    }
}
