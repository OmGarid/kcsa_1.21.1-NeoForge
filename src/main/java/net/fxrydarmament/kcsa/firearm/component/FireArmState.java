package net.fxrydarmament.kcsa.firearm.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record FireArmState(int currentAmmo, String currentFireMode) {

    public static final Codec<FireArmState> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("current_ammo").forGetter(FireArmState::currentAmmo),
                    Codec.STRING.fieldOf("current_fire_mode").forGetter(FireArmState::currentFireMode)
            ).apply(instance, FireArmState::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, FireArmState> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, FireArmState::currentAmmo,
                    ByteBufCodecs.STRING_UTF8, FireArmState::currentFireMode,
                    FireArmState::new
            );

    public FireArmState withAmmo(int newAmmo) {
        return new FireArmState(newAmmo, this.currentFireMode);
    }

    public FireArmState withFireMode(String newFireMode) {
        return new FireArmState(this.currentAmmo, newFireMode);
    }
}