package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.block.tubes.ModuleRedstone;
import me.desht.pneumaticcraft.common.block.tubes.ModuleRedstone.EnumRedstoneDirection;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client to update server-side settings when redstone module GUI is closed
 */
public class PacketSyncRedstoneModuleToServer extends LocationIntPacket {
    private byte side;
    private byte op;
    private byte ourColor;
    private byte otherColor;
    private byte constantVal;
    private boolean invert;
    private boolean input;

    @SuppressWarnings("unused")
    public PacketSyncRedstoneModuleToServer() {
    }

    public PacketSyncRedstoneModuleToServer(ModuleRedstone module) {
        super(module.getTube().getPos());

        this.input = module.getRedstoneDirection() == EnumRedstoneDirection.INPUT;
        this.side = (byte) module.getDirection().ordinal();
        this.op = (byte) module.getOperation().ordinal();
        this.ourColor = (byte) module.getColorChannel();
        this.otherColor = (byte) module.getOtherColor();
        this.constantVal = (byte) module.getConstantVal();
        this.invert = module.isInverted();
    }

    PacketSyncRedstoneModuleToServer(PacketBuffer buffer) {
        super(buffer);
        side = buffer.readByte();
        input = buffer.readBoolean();
        ourColor = buffer.readByte();
        if (!input) {
            op = buffer.readByte();
            otherColor = buffer.readByte();
            constantVal = buffer.readByte();
            invert = buffer.readBoolean();
        }
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        super.toBytes(buf);
        buf.writeByte(side);
        buf.writeBoolean(input);
        buf.writeByte(ourColor);
        if (!input) {
            buf.writeByte(op);
            buf.writeByte(otherColor);
            buf.writeByte(constantVal);
            buf.writeBoolean(invert);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TileEntity te = ctx.get().getSender().world.getTileEntity(pos);
            if (te instanceof TileEntityPressureTube) {
                TubeModule m = ((TileEntityPressureTube) te).modules[side];
                if (m instanceof ModuleRedstone) {
                    ModuleRedstone mr = (ModuleRedstone) m;
                    EnumRedstoneDirection prev = mr.getRedstoneDirection();
                    mr.setRedstoneDirection(input ? EnumRedstoneDirection.INPUT : EnumRedstoneDirection.OUTPUT);
                    mr.setColorChannel(ourColor);
                    if (!input) {
                        mr.setInverted(invert);
                        mr.setOperation(ModuleRedstone.Operation.values()[op], otherColor, constantVal);
                    }
                    if (prev != mr.getRedstoneDirection()) {
                        TileEntityPressureTube pressureTube = mr.getTube();
                        pressureTube.getWorld().notifyNeighborsOfStateChange(pressureTube.getPos(), pressureTube.getWorld().getBlockState(pressureTube.getPos()).getBlock());
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
