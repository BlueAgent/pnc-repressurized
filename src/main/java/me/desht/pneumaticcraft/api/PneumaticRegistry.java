package me.desht.pneumaticcraft.api;

import me.desht.pneumaticcraft.api.client.IClientRegistry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IPneumaticHelmetRegistry;
import me.desht.pneumaticcraft.api.crafting.IPneumaticRecipeRegistry;
import me.desht.pneumaticcraft.api.drone.IDroneRegistry;
import me.desht.pneumaticcraft.api.fuel.IFuelRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatRegistry;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachineFactory;
import me.desht.pneumaticcraft.api.universal_sensor.ISensorRegistry;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.ModLoadingContext;

/**
 * This class can be used to register and access various things to and from the mod.  All access is via
 * {@link PneumaticRegistry#getInstance()}
 */
public final class PneumaticRegistry {
    private static IPneumaticCraftInterface instance;

    /**
     * Get an instance of the top-level API interface.
     *
     * @return the top-level API interface
     */
    public static IPneumaticCraftInterface getInstance() {
        return instance;
    }

    /**
     * Do not call this method yourself.  For PneumaticCraft internal usage only!
     * @param inter reference to the API interface object
     */
    public static void init(IPneumaticCraftInterface inter) {
        if (instance == null && ModLoadingContext.get().getActiveContainer().getModId().equals(Names.MOD_ID))
            instance = inter;//only allow initialization once; by PneumaticCraft
        else throw new IllegalStateException("Only pneumaticcraft is allowed to call this method!");
    }

    /**
     * Retrieve an instance of this via {@link PneumaticRegistry#getInstance()}
     */
    public interface IPneumaticCraftInterface {

        IPneumaticRecipeRegistry getRecipeRegistry();

        IAirHandlerMachineFactory getAirHandlerMachineFactory();

        IPneumaticHelmetRegistry getHelmetRegistry();

        IDroneRegistry getDroneRegistry();

        IHeatRegistry getHeatRegistry();

        IClientRegistry getGuiRegistry();

        ISensorRegistry getSensorRegistry();

        IItemRegistry getItemRegistry();

        IFuelRegistry getFuelRegistry();

        /**
         * Returns the number of Security Stations that disallow interaction with the given coordinate for the given
         * player. Usually you'd disallow interaction when this returns > 0.
         *
         * @param player the player who is trying to access the block
         * @param pos blockpos of the block being tested
         * @param showRangeLines when true, any Security Station that prevents interaction will show the line grid
         *                       (server --> client update is handled internally).
         * @return the number of Security Stations that disallow interaction for the given player.
         * @throws IllegalArgumentException when called from the client side
         */
        int getProtectingSecurityStations(PlayerEntity player, BlockPos pos, boolean showRangeLines);

        /**
         * Register a fluid that represents liquid XP (e.g. PneumaticCraft Memory Essence, CoFH Essence of
         * Knowledge, or OpenBlocks Liquid XP). This is used in the Aerial Interface to transfer experience to/from
         * the player.
         *
         * @param fluid the fluid to register
         * @param liquidToPointRatio the amount of liquid (in mB) used to get one XP point; use a value of 0 or less to
         *                          unregister this fluid
         */
        void registerXPFluid(Fluid fluid, int liquidToPointRatio);

        /**
         * Get a resource location in PneumaticCraft: Repressurized's namespace.
         * @param path a path
         * @return a resource location
         */
        ResourceLocation RL(String path);
    }

}
