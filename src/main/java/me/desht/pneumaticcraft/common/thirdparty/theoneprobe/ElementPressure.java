package me.desht.pneumaticcraft.common.thirdparty.theoneprobe;

import com.mojang.blaze3d.systems.RenderSystem;
import io.netty.buffer.ByteBuf;
import mcjty.theoneprobe.api.IElement;
import me.desht.pneumaticcraft.client.render.pressure_gauge.PressureGaugeRenderer;
import me.desht.pneumaticcraft.common.capabilities.MachineAirHandler;
import me.desht.pneumaticcraft.common.tileentity.IMinWorkingPressure;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;

public class ElementPressure implements IElement {
    private final float min;
    private final float pressure;
    private final float danger;
    private final float crit;

    private static final float SCALE = 0.7f;

    ElementPressure(TileEntity te, MachineAirHandler airHandler) {
        min = te instanceof IMinWorkingPressure ? ((IMinWorkingPressure) te).getMinWorkingPressure() : 0;
        pressure = airHandler.getPressure();
        danger = airHandler.getDangerPressure();
        crit = airHandler.getCriticalPressure();
    }

    ElementPressure(ByteBuf byteBuf) {
        min = byteBuf.readFloat();
        pressure = byteBuf.readFloat();
        danger = byteBuf.readFloat();
        crit = byteBuf.readFloat();
    }

    @Override
    public void render(int x, int y) {
        RenderSystem.pushMatrix();
        RenderSystem.scaled(SCALE, SCALE, SCALE);
        int x1 = (int)((x + getWidth() / 2) / SCALE);
        int y1 = (int)((y + getHeight() / 2) / SCALE);
        PressureGaugeRenderer.drawPressureGauge(Minecraft.getInstance().fontRenderer, -1, crit, danger, min, pressure, x1, y1,0xFFC0C0C0);
        RenderSystem.popMatrix();
    }

    @Override
    public int getWidth() {
        return 40;
    }

    @Override
    public int getHeight() {
        return 40;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeFloat(min);
        buf.writeFloat(pressure);
        buf.writeFloat(danger);
        buf.writeFloat(crit);
    }

    @Override
    public int getID() {
        return TheOneProbe.elementPressure;
    }
}
