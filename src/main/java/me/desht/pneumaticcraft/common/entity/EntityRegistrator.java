package me.desht.pneumaticcraft.common.entity;

import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityHarvestingDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityLogisticsDrone;
import me.desht.pneumaticcraft.common.entity.projectile.EntityMicromissile;
import me.desht.pneumaticcraft.common.entity.projectile.EntityVortex;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.entity.Entity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.registries.IForgeRegistry;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

@Mod.EventBusSubscriber
public class EntityRegistrator {
    private static int ID = 1;

//    public static void init() {
//        EntityRegistry.registerModEntity(RL("vortex"), EntityVortex.class, "Vortex", 0, PneumaticCraftRepressurized.instance, 80, 1, true);
//        EntityRegistry.registerModEntity(RL("drone"), EntityDrone.class, "Drone", 1, PneumaticCraftRepressurized.instance, 80, 1, true);
//        EntityRegistry.registerModEntity(RL("logistic_drone"), EntityLogisticsDrone.class, "logisticDrone", 2, PneumaticCraftRepressurized.instance, 80, 1, true);
//        EntityRegistry.registerModEntity(RL("harvesting_drone"), EntityHarvestingDrone.class, "harvestingDrone", 3, PneumaticCraftRepressurized.instance, 80, 1, true);
//        EntityRegistry.registerModEntity(RL("micromissile"), EntityMicromissile.class, "micromissile", 4, PneumaticCraftRepressurized.instance, 80, 1, true);
//        EntityRegistry.registerModEntity(RL("ring"), EntityRing.class, Names.MOD_ID + ".ring", 100, PneumaticCraftRepressurized.instance, 80, 1, true);
//    }

    @SubscribeEvent
    public static void onEntityRegister(RegistryEvent.Register<EntityEntry> event) {
        IForgeRegistry<EntityEntry> r = event.getRegistry();
        registerEntity(r, EntityVortex.class, "vortex", "Vortex", 80, 1, true);
        registerEntity(r, EntityDrone.class, "drone", "Drone", 80, 1, true);
        registerEntity(r, EntityLogisticsDrone.class, "logistic_drone", "logisticDrone", 80, 1, true);
        registerEntity(r, EntityHarvestingDrone.class, "harvesting_drone", "harvestingDrone", 80, 1, true);
        registerEntity(r, EntityMicromissile.class, "micromissile", "micromissile", 80, 1, true);
        registerEntity(r, EntityRing.class, "ring", Names.MOD_ID + ".ring", 80, 1, true);
    }

    private static void registerEntity(IForgeRegistry<EntityEntry> reg, Class<? extends Entity> cls, String id, String name, int range, int freq, boolean sendVelocityUpdates) {
        reg.register(EntityEntryBuilder.create()
                .entity(cls)
                .id(RL(id), ID++)
                .name(name)
                .tracker(range, freq, sendVelocityUpdates)
                .build());
    }
}
