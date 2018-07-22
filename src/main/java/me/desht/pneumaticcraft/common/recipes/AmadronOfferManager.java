package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.common.config.AmadronOfferPeriodicConfig;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.inventory.ContainerAmadron;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.*;

public class AmadronOfferManager {
    private static final AmadronOfferManager CLIENT_INSTANCE = new AmadronOfferManager();
    private static final AmadronOfferManager SERVER_INSTANCE = new AmadronOfferManager();

    private final LinkedHashSet<AmadronOffer> staticOffers = new LinkedHashSet<>();
    private final List<AmadronOffer> periodicOffers = new ArrayList<>();
    private final LinkedHashSet<AmadronOffer> selectedPeriodicOffers = new LinkedHashSet<>();
    private final LinkedHashSet<AmadronOffer> allOffers = new LinkedHashSet<>();

    public static AmadronOfferManager getInstance() {
        return FMLCommonHandler.instance().getSide() == Side.SERVER ? SERVER_INSTANCE : CLIENT_INSTANCE;
    }

    public Collection<AmadronOffer> getStaticOffers() {
        return staticOffers;
    }

    public Collection<AmadronOffer> getPeriodicOffers() {
        return periodicOffers;
    }

    public Collection<AmadronOffer> getAllOffers() {
        return allOffers;
    }

    public boolean addStaticOffer(AmadronOffer offer) {
        allOffers.add(offer);
        return staticOffers.add(offer);
    }

    public boolean removeStaticOffer(AmadronOffer offer) {
        allOffers.remove(offer);
        return staticOffers.remove(offer);
    }

    public boolean addPeriodicOffer(AmadronOffer offer) {
        if (periodicOffers.contains(offer)) {
            return false;
        } else {
            periodicOffers.add(offer);
            return true;
        }
    }

    public void removePeriodicOffer(AmadronOffer offer) {
        periodicOffers.remove(offer);
    }

    public boolean hasOffer(AmadronOffer offer) {
        return allOffers.contains(offer);
    }

    public void recompileOffers() {
        allOffers.clear();
        allOffers.addAll(staticOffers);
        allOffers.addAll(selectedPeriodicOffers);
    }

    /**
     * Called client-side to update the client about the available offers.
     */
    @SideOnly(Side.CLIENT)
    public void setOffers(Collection<AmadronOffer> newOffers) {
        allOffers.clear();
        allOffers.addAll(newOffers);
    }

    /**
     * Gets the offer that equals() a copy.
     *
     * @param offer
     * @return
     */
    public AmadronOffer get(AmadronOffer offer) {
        for (AmadronOffer o : allOffers) {
            if (o.equals(offer)) return o;
        }
        return null;
    }

    public int countOffers(String playerId) {
        int count = 0;
        for (AmadronOffer offer : allOffers) {
            if (offer instanceof AmadronOfferCustom && ((AmadronOfferCustom) offer).getPlayerId().equals(playerId))
                count++;
        }
        return count;
    }

    public void tryRestockCustomOffers() {
        for (AmadronOffer offer : allOffers) {
            if (offer instanceof AmadronOfferCustom) {
                AmadronOfferCustom custom = (AmadronOfferCustom) offer;
                TileEntity input = custom.getProvidingTileEntity();
                TileEntity output = custom.getReturningTileEntity();
                int possiblePickups = ContainerAmadron.capShoppingAmount(custom.invert(), 50,
                        getItemHandler(input), getItemHandler(output),
                        getFluidHandler(input), getFluidHandler(output),
                        null);
                if (possiblePickups > 0) {
                    BlockPos pos = new BlockPos(input.getPos().getX(), input.getPos().getY(), input.getPos().getZ());
                    EntityDrone drone = ContainerAmadron.retrieveOrderItems(custom, possiblePickups, input.getWorld(), pos, input.getWorld(), pos);
                    if (drone != null) {
                        drone.setHandlingOffer(custom.copy(), possiblePickups, ItemStack.EMPTY, "Restock");
                    }
                }
                custom.invert();
                custom.payout();
            }
        }
    }

    static IItemHandler getItemHandler(TileEntity te) {
        return te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null) ?
                te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null) : null;
    }

    static IFluidHandler getFluidHandler(TileEntity te) {
        return te != null && te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null) ?
                te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null) : null;
    }

    public void shufflePeriodicOffers() {
        Random rand = new Random();
        allOffers.removeAll(selectedPeriodicOffers);
        selectedPeriodicOffers.clear();
        int toBeSelected = Math.min(AmadronOfferPeriodicConfig.offersPer, periodicOffers.size());
        while (selectedPeriodicOffers.size() < toBeSelected) {
            selectedPeriodicOffers.add(periodicOffers.get(rand.nextInt(periodicOffers.size())));
        }
        allOffers.addAll(selectedPeriodicOffers);
    }
}
