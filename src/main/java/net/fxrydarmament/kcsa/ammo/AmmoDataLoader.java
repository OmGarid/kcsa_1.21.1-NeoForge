package net.fxrydarmament.kcsa.ammo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fxrydarmament.kcsa.FXRYDArmament;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public class AmmoDataLoader extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<ResourceLocation, AmmoData> LOADED_AMMO = new HashMap<>();

    public AmmoDataLoader() {
        super(GSON, "ammo"); // reads from data/<namespace>/ammo/*.json
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profiler) {
        LOADED_AMMO.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : jsonMap.entrySet()) {
            ResourceLocation ammoId = entry.getKey();
            JsonObject json = entry.getValue().getAsJsonObject();

            try {
                String ammoName = json.get("ammo_name").getAsString();
                boolean is3d = json.has("is_3d") && json.get("is_3d").getAsBoolean();

                LOADED_AMMO.put(ammoId, new AmmoData(ammoId, ammoName, is3d));
            } catch (Exception e) {
                FXRYDArmament.LOGGER.error("Failed to load ammo data from {}: {}", ammoId, e.getMessage());
            }
        }

        FXRYDArmament.LOGGER.info("Loaded {} ammo type(s).", LOADED_AMMO.size());
    }

    public static AmmoData get(ResourceLocation ammoId) {
        return LOADED_AMMO.get(ammoId);
    }

    public static Map<ResourceLocation, AmmoData> getAll() {return LOADED_AMMO;}
}