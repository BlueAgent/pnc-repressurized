package me.desht.pneumaticcraft.common.thirdparty;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public enum ModdedWrenchUtils {
    INSTANCE;

    @ObjectHolder("thermalfoundation:wrench")
    private static Item CRESCENT_HAMMER = null;
    @ObjectHolder("rftools:smartwrench")
    private static Item SMART_WRENCH = null;
    @ObjectHolder("immersiveengineering:tool")
    private static Item IMMERSIVE_TOOL = null;
    @ObjectHolder("appliedenergistics2:certus_quartz_wrench")
    private static Item AE2_CERTUS_WRENCH = null;
    @ObjectHolder("appliedenergistics2:nether_quartz_wrench")
    private static Item AE2_NETHER_WRENCH = null;
    @ObjectHolder("enderio:item_yeta_wrench")
    private static Item YETA_WRENCH = null;
    @ObjectHolder("buildcraftcore:wrench")
    private static Item BC_WRENCH = null;
    @ObjectHolder("teslacorelib:wrench")
    private static Item TESLA_WRENCH = null;
    @ObjectHolder("ic2:wrench")
    private static Item IC2_WRENCH = null;
    @ObjectHolder("chiselsandbits:wrench_wood")
    private static Item CB_WRENCH_WOOD = null;

    private final Set<Item> wrenches = new HashSet<>();

    public static ModdedWrenchUtils getInstance() {
        return INSTANCE;
    }

    public void registerThirdPartyWrenches() {
        registerWrench(CRESCENT_HAMMER);
        registerWrench(SMART_WRENCH);
        registerWrench(IMMERSIVE_TOOL);
        registerWrench(AE2_CERTUS_WRENCH);
        registerWrench(AE2_NETHER_WRENCH);
        registerWrench(YETA_WRENCH);
        registerWrench(BC_WRENCH);
        registerWrench(TESLA_WRENCH);
        registerWrench(IC2_WRENCH);
        registerWrench(CB_WRENCH_WOOD);
    }

    private void registerWrench(Item wrench) {
        if (wrench != null) wrenches.add(wrench);
    }

    /**
     * Check if the given item is a known 3rd party modded wrench
     *
     * @param stack the item to check
     * @return true if it's a modded wrench, false otherwise
     */
    public boolean isModdedWrench(@Nonnull ItemStack stack) {
        return wrenches.contains(stack.getItem());
    }

}
