package me.desht.pneumaticcraft.common.thirdparty.computercraft;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.common.thirdparty.IRegistryListener;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ComputerCraft extends OpenComputers implements IRegistryListener {
    @GameRegistry.ObjectHolder(ModIds.COMPUTERCRAFT + ":peripheral")
    public static final Block MODEM = null;

    @Override
    public void preInit() {
        ThirdPartyManager.computerCraftLoaded = true;
        PneumaticRegistry.getInstance().getHelmetRegistry().registerBlockTrackEntry(new BlockTrackEntryPeripheral());
        super.preInit();
    }

    @Override
    public void init() {
        if (Loader.isModLoaded(ModIds.OPEN_COMPUTERS)) super.init();
    }

    @Override
    public void onItemRegistry(Item item) {
    }

    @Override
    public void onBlockRegistry(Block block) {
        if (block instanceof IPeripheralProvider) {
            ComputerCraftAPI.registerPeripheralProvider((IPeripheralProvider) block);
        }
    }

}
