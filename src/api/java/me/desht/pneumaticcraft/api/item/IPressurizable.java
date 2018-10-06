package me.desht.pneumaticcraft.api.item;

import net.minecraft.item.ItemStack;

/**
 * Implement this interface on items which support the concept of pressure. Any item implementing this interface will
 * be able to (dis)charge in a Charging Station.
 * <p>
 * Don't use this for tile entities - see instead {@link me.desht.pneumaticcraft.api.tileentity.IPneumaticMachine}
 */
public interface IPressurizable {
    /**
     * This method should return the current pressure of the ItemStack given.
     *
     * @param iStack Stack the pressure is asked from.
     * @return Pressure in bar.
     */
    float getPressure(ItemStack iStack);

    /**
     * This method is used to charge or discharge a pneumatic item. When the
     * value is negative the item should be discharging.
     *
     * @param iStack the ItemStack which has to be (dis)charged.
     * @param amount amount in mL that the item is (dis)charging.
     */
    void addAir(ItemStack iStack, int amount);

    /**
     * This method should return the maximum pressure of a pneumatic item. If it
     * has reached this maximum, it won't explode, but it wouldn't (try to)
     * charge either.
     *
     * @param iStack the stack from which the maximum pressure is asked.
     * @return maximum pressure in bar.
     */
    float maxPressure(ItemStack iStack);

    /**
     * Get the volume for this item, i.e. the amount of air stored at a pressure
     * of 1 bar.  It follows that the current air stored in an item is
     * {@code getPressure(stack) * getVolume(stack)}, and the maximum air storage
     * is {@code getMaxPressure(stack) * getVolume(stack)}.
     *
     * @param iStack the item
     * @return the item's air volume
     */
    int getVolume(ItemStack iStack);
}
