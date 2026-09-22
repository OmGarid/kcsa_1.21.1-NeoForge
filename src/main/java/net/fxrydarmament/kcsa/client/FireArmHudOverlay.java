package net.fxrydarmament.kcsa.client;

import net.fxrydarmament.kcsa.firearm.FireArmData;
import net.fxrydarmament.kcsa.firearm.FireArmDataLoader;
import net.fxrydarmament.kcsa.firearm.FireArmItem;
import net.fxrydarmament.kcsa.firearm.component.FireArmState;
import net.fxrydarmament.kcsa.firearm.component.FireArmComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = "fxrydarmament", value = Dist.CLIENT)
public class FireArmHudOverlay {

    private static final int HUD_LINE_HEIGHT = 10; // height of one text line (default MC font is ~9-10px)
    private static final int PADDING_ABOVE = 4;     // gap above the element it sits on (health/armor bar)

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui) return;

        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof FireArmItem)) return;

        ResourceLocation weaponId = held.get(FireArmComponents.FIREARM_ID.get());
        FireArmState state = held.get(FireArmComponents.FIREARM_STATE.get());
        if (weaponId == null || state == null) return;

        FireArmData data = FireArmDataLoader.get(weaponId);
        if (data == null) return;

        GuiGraphics gg = event.getGuiGraphics();

        int leftHeight = mc.gui.leftHeight; // already includes health+armor when they're rendering
        int hudHeight = HUD_LINE_HEIGHT * 2; // 2 baris: nama senjata + ammo count

        int hotbarLeftEdge = gg.guiWidth() / 2 - 91; // sisi kiri sprite hotbar vanilla
        int y = gg.guiHeight() - leftHeight - hudHeight - PADDING_ABOVE;
        int x = hotbarLeftEdge;

        String ammoText = "Current Ammo: " + state.currentAmmo() + " / " + data.getMagazineCapacity();
        String fireModeText = "Fire Mode: " + state.currentFireMode();

        gg.drawString(mc.font, data.getWeaponName(), x, y, 0xFFFFFF, true);
        y += HUD_LINE_HEIGHT;

        int ammoColor = state.currentAmmo() == 0 ? 0xFF5555 : 0xAAAAAA;
        if (state.currentAmmo() > 0 && state.currentAmmo() < 15) {ammoColor = 0xFFE300;}
        gg.drawString(mc.font, ammoText, x, y, ammoColor, true);
        y += HUD_LINE_HEIGHT;

        gg.drawString(mc.font, fireModeText, x, y, 0xAAAAAA, true);
        y += HUD_LINE_HEIGHT;
    }
}