package me.desht.pneumaticcraft.common.entity.semiblock;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class EntityLogisticsPassiveProvider extends EntityLogisticsActiveProvider {
    public static EntityLogisticsPassiveProvider createPassive(EntityType<EntityLogisticsPassiveProvider> type, World world) {
        return new EntityLogisticsPassiveProvider(type, world);
    }

    private EntityLogisticsPassiveProvider(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Override
    public int getColor() {
        return 0xFFFF0000;
    }

    @Override
    public boolean shouldProvideTo(int level) {
        return level > 2;
    }
}
