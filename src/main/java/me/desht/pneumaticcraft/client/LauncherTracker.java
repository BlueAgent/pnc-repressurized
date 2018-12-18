package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.client.render.RenderProgressBar;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketChestplateLauncher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumHandSide;

public enum LauncherTracker {
    INSTANCE;

    public static final int MAX_PROGRESS = 20; // ticks

    private int launcherProgress = 0;

    public int getLauncherProgress() {
        return launcherProgress;
    }

    public void startCharging() {
        if (KeyHandler.getInstance().keybindLauncher.isKeyDown()) {
            launcherProgress++;
        }
    }

    public void chargeLauncher() {
        if (launcherProgress > 0 && launcherProgress < MAX_PROGRESS) {
            launcherProgress++;
        }
    }

    public void trigger() {
        NetworkHandler.sendToServer(new PacketChestplateLauncher((float) launcherProgress / (float) MAX_PROGRESS));
        launcherProgress = 0;
    }

    public void render(ScaledResolution sr, float partialTicks) {
        int progress = LauncherTracker.INSTANCE.getLauncherProgress() * 100 / LauncherTracker.MAX_PROGRESS;
        int progress1 = (LauncherTracker.INSTANCE.getLauncherProgress() + 1) * 100 / LauncherTracker.MAX_PROGRESS;
        int p = Math.min(progress, progress + (int)((progress1 - progress) * partialTicks));
        GlStateManager.pushMatrix();
        boolean left = Minecraft.getMinecraft().player.getPrimaryHand() == EnumHandSide.LEFT;
        GlStateManager.translate(left ? sr.getScaledWidth() - 30 : 30, sr.getScaledHeight() - 30, -90);
        if (left) GlStateManager.scale(-1, 1, 1);
        GlStateManager.rotate(-60, 0, 0, 1);
        RenderProgressBar.render(0, 0, sr.getScaledWidth_double() / 6 - 30,
                12, 0, p, 0xAA00AAAA);
        GlStateManager.popMatrix();
    }
}
