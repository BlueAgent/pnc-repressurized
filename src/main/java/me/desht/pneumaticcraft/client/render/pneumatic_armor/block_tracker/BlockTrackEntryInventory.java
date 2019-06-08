package me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IBlockTrackEntry;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.InventoryTrackEvent;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketDescriptionPacketRequest;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public class BlockTrackEntryInventory implements IBlockTrackEntry {
    @Override
    public boolean shouldTrackWithThisEntry(IBlockAccess world, BlockPos pos, IBlockState state, TileEntity te) {
        if (te instanceof TileEntityChest) {
            TileEntityChest chest = (TileEntityChest) te;
            if (chest.adjacentChestXNeg != null || chest.adjacentChestZNeg != null) return false;
        }

        return te != null
                && !TrackerBlacklistManager.isInventoryBlacklisted(te)
                && IBlockTrackEntry.hasCapabilityOnAnyFace(te, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                && !MinecraftForge.EVENT_BUS.post(new InventoryTrackEvent(te));
    }

    @Override
    public boolean shouldBeUpdatedFromServer(TileEntity te) {
        if (te instanceof TileEntityChest) {
            TileEntityChest chest = (TileEntityChest) te;
            if (chest.adjacentChestXPos != null)
                NetworkHandler.sendToServer(new PacketDescriptionPacketRequest(chest.adjacentChestXPos.getPos()));
            if (chest.adjacentChestZPos != null)
                NetworkHandler.sendToServer(new PacketDescriptionPacketRequest(chest.adjacentChestZPos.getPos()));
        }
        return true;
    }

    @Override
    public int spamThreshold() {
        return 16;
    }

    @Override
    public void addInformation(World world, BlockPos pos, TileEntity te, EnumFacing face, List<String> infoList) {
        try {
            IItemHandler inventory = IOHelper.getInventoryForTE(te, face);
            if (inventory != null) {
                boolean empty = true;
                ItemStack[] inventoryStacks = new ItemStack[inventory.getSlots()];
                for (int i = 0; i < inventory.getSlots(); i++) {
                    ItemStack iStack = inventory.getStackInSlot(i);
                    if (!iStack.isEmpty()) {
                        empty = false;
                    }
                    inventoryStacks[i] = iStack;
                }
                if (empty) {
                    infoList.add("Contents: Empty");
                } else {
                    infoList.add("Contents:");
                    PneumaticCraftUtils.sortCombineItemStacksAndToString(infoList, inventoryStacks);
                }
            }
        } catch (Throwable e) {
            TrackerBlacklistManager.addInventoryTEToBlacklist(te, e);
        }
    }

    @Override
    public String getEntryName() {
        return "blockTracker.module.inventories";
    }
}
