package me.desht.pneumaticcraft.common.thirdparty.waila;

import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerDataProvider;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.List;

public class EntityProvider {

    public static class Data implements IServerDataProvider<Entity> {
        @Override
        public void appendServerData(CompoundNBT compoundNBT, ServerPlayerEntity serverPlayerEntity, World world, Entity entity) {
            entity.getCapability(PNCCapabilities.AIR_HANDLER_CAPABILITY)
                    .ifPresent(h -> compoundNBT.putFloat("Pressure", h.getPressure()));
        }
    }

    public static class Component implements IEntityComponentProvider {
        @Override
        public void appendBody(List<ITextComponent> tooltip, IEntityAccessor accessor, IPluginConfig config) {
            float pressure = accessor.getServerData().getFloat("Pressure");
            tooltip.add(new TranslationTextComponent("gui.tooltip.pressure", PneumaticCraftUtils.roundNumberTo(pressure, 1)));
        }
    }
}
