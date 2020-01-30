package me.desht.pneumaticcraft.common.heat.behaviour;

import me.desht.pneumaticcraft.api.heat.HeatBehaviour;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.Validate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public enum HeatBehaviourManager {
    INSTANCE;

    private final Map<ResourceLocation, Supplier<? extends HeatBehaviour>> behaviourRegistry = new HashMap<>();

    public static HeatBehaviourManager getInstance() {
        return INSTANCE;
    }

    public void reload() {
        behaviourRegistry.clear();

        registerBehaviour(HeatBehaviourFurnace.ID, HeatBehaviourFurnace::new);
        registerBehaviour(HeatBehaviourHeatFrame.ID, HeatBehaviourHeatFrame::new);

        // this handles all custom non-tile-entity blocks and fluids, both vanilla and modded
        registerBehaviour(HeatBehaviourCustomTransition.ID, HeatBehaviourCustomTransition::new);
    }

    public void registerBehaviour(ResourceLocation id, Supplier<? extends HeatBehaviour> behaviour) {
        Validate.notNull(behaviour);

        Supplier<? extends HeatBehaviour> existing = behaviourRegistry.put(id, behaviour);
        if (existing != null) Log.warning("Overriding heat behaviour " + id);
    }

    public <T extends HeatBehaviour> T createBehaviour(ResourceLocation id) {
        Supplier<? extends HeatBehaviour> behaviour = behaviourRegistry.get(id);
        if (behaviour != null) {
            //noinspection unchecked
            return (T) behaviour.get();
        } else {
            Log.warning("No heat behaviour found for id: " + id);
            return null;
        }
    }

    public void addHeatBehaviours(World world, BlockPos pos, Direction direction, IHeatExchangerLogic logic, List<HeatBehaviour> list) {
        for (Supplier<? extends HeatBehaviour> sup : behaviourRegistry.values()) {
            HeatBehaviour behaviour = sup.get().initialize(logic, world, pos, direction);
            if (behaviour.isApplicable()) {
                list.add(behaviour);
            }
        }
    }
}
