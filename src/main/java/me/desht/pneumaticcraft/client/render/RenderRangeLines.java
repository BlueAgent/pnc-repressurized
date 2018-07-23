package me.desht.pneumaticcraft.client.render;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class RenderRangeLines {
    private final List<RenderProgressingLine> rangeLines = new ArrayList<>();
    private int rangeLinesTimer = 0;
    private static Random rand = new Random();
    private final int color;
    private BlockPos pos;

    public RenderRangeLines(int color) {
        this(color, null);
    }

    public RenderRangeLines(int color, BlockPos pos) {
        this.color = color;
        this.pos = pos;
    }

    public void resetRendering(double range) {
        rangeLinesTimer = 200;

        rangeLines.clear();
        double renderRange = range + 0.5D;
        for (int i = 0; i < range * 16 + 8; i++) {
            //Add the vertical lines of the walls
            rangeLines.add(new RenderProgressingLine(-renderRange + i / 8D, -renderRange + 1, -renderRange, -renderRange + i / 8D, renderRange + 1, -renderRange));
            rangeLines.add(new RenderProgressingLine(renderRange - i / 8D, -renderRange + 1, renderRange, renderRange - i / 8D, renderRange + 1, renderRange));
            rangeLines.add(new RenderProgressingLine(-renderRange, -renderRange + 1, renderRange - i / 8D, -renderRange, renderRange + 1, renderRange - i / 8D));
            rangeLines.add(new RenderProgressingLine(renderRange, -renderRange + 1, -renderRange + i / 8D, renderRange, renderRange + 1, -renderRange + i / 8D));

            //Add the horizontal lines of the walls
            rangeLines.add(new RenderProgressingLine(-renderRange, -renderRange + i / 8D + 1, -renderRange, -renderRange, -renderRange + i / 8D + 1, renderRange));
            rangeLines.add(new RenderProgressingLine(renderRange, -renderRange + i / 8D + 1, -renderRange, renderRange, -renderRange + i / 8D + 1, renderRange));
            rangeLines.add(new RenderProgressingLine(-renderRange, renderRange - i / 8D + 1, -renderRange, renderRange, renderRange - i / 8D + 1, -renderRange));
            rangeLines.add(new RenderProgressingLine(-renderRange, -renderRange + i / 8D + 1, renderRange, renderRange, -renderRange + i / 8D + 1, renderRange));

            //Add the roof and floor
            rangeLines.add(new RenderProgressingLine(renderRange - i / 8D, -renderRange + 1, -renderRange, renderRange - i / 8D, -renderRange + 1, renderRange));
            rangeLines.add(new RenderProgressingLine(renderRange - i / 8D, renderRange + 1, -renderRange, renderRange - i / 8D, renderRange + 1, renderRange));
            rangeLines.add(new RenderProgressingLine(-renderRange, -renderRange + 1, -renderRange + i / 8D, renderRange, -renderRange + 1, -renderRange + i / 8D));
            rangeLines.add(new RenderProgressingLine(-renderRange, renderRange + 1, -renderRange + i / 8D, renderRange, renderRange + 1, -renderRange + i / 8D));

        }
    }

    public boolean isCurrentlyRendering() {
        return rangeLines.size() > 0;
    }

    public void update() {
        if (rangeLinesTimer > 0) {
            rangeLinesTimer--;
            for (RenderProgressingLine line : rangeLines) {
                if (line.getProgress() > 0.005F || rand.nextInt(15) == 0) {
                    line.incProgress(0.025F);
                }
            }
        } else {
            Iterator<RenderProgressingLine> iterator = rangeLines.iterator();
            while (iterator.hasNext()) {
                RenderProgressingLine line = iterator.next();
                if (line.getProgress() > 0.005F) {
                    line.incProgress(0.025F);
                }
                if (rand.nextInt(10) == 0) {
                    iterator.remove();
                }
            }
        }
    }

    public void render() {
        if (rangeLines.isEmpty()) return;

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderUtils.glColorHex(color);
        EntityPlayer player = PneumaticCraftRepressurized.proxy.getClientPlayer();
        if (pos != null) {
            GlStateManager.translate(pos.getX() - player.posX + 0.5, pos.getY() - player.posY + 0.5, pos.getZ() - player.posZ + 0.5);
        }
        GlStateManager.glLineWidth(2.0F);
        for (RenderProgressingLine line : rangeLines) {
            line.render();
        }
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }
}
