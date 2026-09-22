package net.fxrydarmament.kcsa.firearm;

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

/*
    Data Reader

    This class reads firearm data from JSON files located at:
    root://src/main/resources/data/fxrydarmament/firearm/*.json

    The parsed data is stored as a new FireArmData object (same directory as this class).
 */

public class FireArmDataLoader extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<ResourceLocation, FireArmData> LOADED_WEAPONS = new HashMap<>();

    // Fallback values used when a JSON entry omits an optional field
    private static final int DEFAULT_DRAW_TIME = 10;
    private static final int DEFAULT_HOLSTER_TIME = 10;
    private static final float DEFAULT_RECOIL_PITCH = 2.0f;
    private static final float DEFAULT_RECOIL_YAW = 0.5f;
    private static final float DEFAULT_RECOIL_RECOVERY = 0.15f;
    private static final float DEFAULT_RECOIL_RANDOMNESS = 0.2f;
    private static final float DEFAULT_SOUND_VOLUME = 1.0F;
    private static final float DEFAULT_SOUND_PITCH = 1.0F;

    public FireArmDataLoader() {
        super(GSON, "firearm");
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> jsonMap,
            ResourceManager resourceManager,
            ProfilerFiller profiler
    ) {
        LOADED_WEAPONS.clear();

        // Check for any firearm data files (json) available in the designated directory
        for (Map.Entry<ResourceLocation, JsonElement> entry : jsonMap.entrySet()) {
            ResourceLocation weaponId = entry.getKey();
            JsonObject json = entry.getValue().getAsJsonObject();

            try {
                String weaponName = json.get("weapon_name").getAsString();

                String weaponDesc = json.has("weapon_desc")
                        ? json.get("weapon_desc").getAsString()
                        : "";

                int magazineCapacity = json.get("magazine_capacity").getAsInt();
                int damage = json.get("damage").getAsInt();
                int fireRate = json.get("fire_rate").getAsInt();
                int reloadTime = json.get("reload_time").getAsInt();
                int reloadEmptyTime = json.get("reload_empty_time").getAsInt();
                int drawTime = json.has("draw_time") ? json.get("draw_time").getAsInt() : DEFAULT_DRAW_TIME;
                int holsterTime = json.has("holster_time") ? json.get("holster_time").getAsInt() : DEFAULT_HOLSTER_TIME;

                float recoilPitch = json.has("recoil_pitch") ? json.get("recoil_pitch").getAsFloat() : DEFAULT_RECOIL_PITCH;
                float recoilYaw = json.has("recoil_yaw") ? json.get("recoil_yaw").getAsFloat() : DEFAULT_RECOIL_YAW;
                float recoilRecovery = json.has("recoil_recovery") ? json.get("recoil_recovery").getAsFloat() : DEFAULT_RECOIL_RECOVERY;
                float recoilRandomness = json.has("recoil_randomness") ? json.get("recoil_randomness").getAsFloat() : DEFAULT_RECOIL_RANDOMNESS;
                ResourceLocation ammoType = json.has("ammo_type")
                        ? ResourceLocation.parse(json.get("ammo_type").getAsString())
                        : null;

                Map<String, FireArmSoundData> sounds = new HashMap<>();

                if (json.has("sounds")) {
                    JsonObject soundsJson = json.getAsJsonObject("sounds");

                    for (Map.Entry<String, JsonElement> soundEntry : soundsJson.entrySet()) {
                        JsonObject soundJson = soundEntry.getValue().getAsJsonObject();

                        float volume = soundJson.has("volume")
                                ? soundJson.get("volume").getAsFloat()
                                : DEFAULT_SOUND_VOLUME;

                        float pitchMin = soundJson.has("pitch_min")
                                ? soundJson.get("pitch_min").getAsFloat()
                                : DEFAULT_SOUND_PITCH;

                        float pitchMax = soundJson.has("pitch_max")
                                ? soundJson.get("pitch_max").getAsFloat()
                                : DEFAULT_SOUND_PITCH;

                        sounds.put(
                                soundEntry.getKey(),
                                new FireArmSoundData(
                                        volume,
                                        pitchMin,
                                        pitchMax
                                )
                        );
                    }
                }

                // Create the firearm data object
                FireArmData data = new FireArmData(
                        weaponId,
                        weaponName,
                        weaponDesc,
                        magazineCapacity,
                        damage,
                        fireRate,
                        reloadTime,
                        reloadEmptyTime,
                        recoilPitch,
                        recoilYaw,
                        recoilRecovery,
                        recoilRandomness,
                        sounds,
                        drawTime,
                        holsterTime,
                        ammoType
                );

                LOADED_WEAPONS.put(weaponId, data);

            } catch (Exception e) {
                FXRYDArmament.LOGGER.error("Failed to load firearm data from {}: {}", weaponId, e.getMessage());
            }
        }

        FXRYDArmament.LOGGER.info("Loaded {} firearm(s).", LOADED_WEAPONS.size());
    }

    public static FireArmData get(ResourceLocation weaponId) {
        return LOADED_WEAPONS.get(weaponId);
    }

    public static Map<ResourceLocation, FireArmData> getAll() {
        return LOADED_WEAPONS;
    }
}