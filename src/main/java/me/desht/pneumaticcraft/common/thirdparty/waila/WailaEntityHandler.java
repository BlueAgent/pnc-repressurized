package me.desht.pneumaticcraft.common.thirdparty.waila;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaEntityAccessor;
import mcp.mobius.waila.api.IWailaEntityProvider;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

public class WailaEntityHandler implements IWailaEntityProvider {
    @Nonnull
    @Override
    public List<String> getWailaBody(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {
        addTipToEntity(currenttip, accessor);
        return currenttip;
    }

    private static void addTipToEntity(List<String> currenttip, IWailaEntityAccessor accessor) {
        if (accessor.getEntity() instanceof IPressurizable) {
            float pressure = accessor.getNBTData().getFloat("Pressure");
            currenttip.add("Pressure: " + TextFormatting.WHITE + PneumaticCraftUtils.roundNumberTo(pressure, 1) + " bar");
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, Entity ent, NBTTagCompound tag, World world) {
        if (ent instanceof IPressurizable) {
            tag.setFloat("Pressure", ((IPressurizable) ent).getPressure(ItemStack.EMPTY));
        }
        return tag;
    }
}
