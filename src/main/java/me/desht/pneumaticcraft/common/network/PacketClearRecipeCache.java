package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server when recipes are reloaded; clear our local cache of machine recipes
 */
public class PacketClearRecipeCache {
    public PacketClearRecipeCache() {
    }

    public void toBytes(PacketBuffer buf) {
    }

    public PacketClearRecipeCache(PacketBuffer buffer) {
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
        ctx.get().enqueueWork(PneumaticCraftRecipeType::clearCachedRecipes);
    }
}
