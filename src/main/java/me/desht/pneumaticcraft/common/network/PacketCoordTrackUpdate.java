package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client when player right-clicks a block to select a coord tracker target
 */
public class PacketCoordTrackUpdate extends LocationIntPacket {

    private ResourceLocation dimensionID;

    public PacketCoordTrackUpdate() {
    }

    public PacketCoordTrackUpdate(World world, BlockPos pos) {
        super(pos);
        dimensionID = world.func_234923_W_().func_240901_a_();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        super.toBytes(buffer);
        buffer.writeResourceLocation(dimensionID);
    }

    public PacketCoordTrackUpdate(PacketBuffer buffer) {
        super(buffer);
        dimensionID = buffer.readResourceLocation();
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ItemStack stack = ctx.get().getSender().getItemStackFromSlot(EquipmentSlotType.HEAD);
            if (stack.getItem() instanceof ItemPneumaticArmor) {
                RegistryKey<World> worldKey = RegistryKey.func_240903_a_(Registry.WORLD_KEY, dimensionID);
                ItemPneumaticArmor.setCoordTrackerPos(stack, GlobalPos.func_239648_a_(worldKey, pos));
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
