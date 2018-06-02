package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemStackHandler;

/**
 * An ItemStackHandler which also supports comparator signal level calculation.
 * Smart enough to only recalculate the signal when the contents have changed.
 */
public class ComparatorItemStackHandler extends ItemStackHandler {
    private int signalLevel = -1;  // -1 indicates recalc needed

    public ComparatorItemStackHandler(int invSize) {
        super(invSize);
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        signalLevel = -1;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        signalLevel = -1;
    }

    /**
     * Get the comparator level for this inventory.  Follows the same rules as Container#calcRedstoneLevel()
     *
     * @return a redstone signal level based on the inventory fullness
     */
    public int getComparatorValue() {
        if (signalLevel < 0) {
            signalLevel = PneumaticCraftUtils.calcRedstoneFromHandler(this);
        }
        return signalLevel;
    }

    /**
     * Force a recalculation of the comparator level.  Recalculation will be done the next time
     * getComparatorValue() is called.
     */
    public void recalcComparatorValue() {
        signalLevel = -1;
    }
}
