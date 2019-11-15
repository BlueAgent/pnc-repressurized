package me.desht.pneumaticcraft.common.thirdparty.thaumcraft;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;

public class Thaumcraft implements IThirdParty {
    @Override
    public void preInit() {
        PneumaticRegistry.getInstance().getHelmetRegistry().registerBlockTrackEntry(new BlockTrackEntryThaumcraft());
    }

    // no init() method needed: recipe registration is done vis JSON

    @Override
    public void postInit() {
        // it appears aspect levels for common vanilla items are 5x what they were in TC4 (1.7.10)
        // scaling these accordingly...

//        AspectList transAndCapAspects = new AspectList().add(Aspect.ENERGY, 10).add(Aspect.MECHANISM, 10).add(Aspect.METAL, 30);
//        AspectList pcbAspects = new AspectList().add(Aspect.ENERGY, 5).add(Aspect.ORDER, 10).add(Aspect.METAL, 30);
//
//        ThaumcraftApi.registerObjectTag(new ItemStack(ModItems.TURBINE_BLADE), new AspectList()
//                .add(Aspect.METAL, 15)
//                .add(Aspect.MOTION, 10)
//                .add(Aspect.ENERGY, 20));
//        ThaumcraftApi.registerObjectTag(new ItemStack(ModItems.CAPACITOR), transAndCapAspects);
//        ThaumcraftApi.registerObjectTag(new ItemStack(ModItems.TRANSISTOR), transAndCapAspects);
//        ThaumcraftApi.registerObjectTag(new ItemStack(ModItems.EMPTY_PCB), pcbAspects);
//        ThaumcraftApi.registerObjectTag(new ItemStack(ModItems.UNASSEMBLED_PCB), pcbAspects);
//        ThaumcraftApi.registerObjectTag(new ItemStack(ModItems.AIR_CANISTER), new AspectList().add(Aspect.METAL, 30).add(Aspect.ENERGY, 20).add(Aspect.AIR, 20));
    }
}
