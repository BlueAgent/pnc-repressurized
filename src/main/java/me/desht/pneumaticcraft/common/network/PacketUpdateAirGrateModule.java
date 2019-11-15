package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.block.tubes.ModuleAirGrate;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;

public class PacketUpdateAirGrateModule extends PacketUpdateTubeModule<PacketUpdateAirGrateModule> {
    private String entityFilter;

    public PacketUpdateAirGrateModule() {
    }

    public PacketUpdateAirGrateModule(TubeModule module, String entityFilter) {
        super(module);
        this.entityFilter = entityFilter;
    }

    public PacketUpdateAirGrateModule(PacketBuffer buffer) {
        super(buffer);
        entityFilter = buffer.readString();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        super.toBytes(buffer);
        buffer.writeString(entityFilter);
    }

    @Override
    protected void onModuleUpdate(TubeModule module, PlayerEntity player) {
        if (module instanceof ModuleAirGrate) {
            ((ModuleAirGrate) module).setEntityFilter(entityFilter);
        }
    }
}
