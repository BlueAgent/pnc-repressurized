package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class SlotUpgrade extends SlotItemHandler {

    private final TileEntityBase te;

    SlotUpgrade(TileEntityBase inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn.getUpgradeHandler(), index, xPosition, yPosition);
        te = inventoryIn;
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return te.getUpgradeHandler().isItemValid(getSlotIndex(), stack);
    }

    @Override
    public void onSlotChanged() {
        te.getUpgradeCache().invalidate();
    }
}
