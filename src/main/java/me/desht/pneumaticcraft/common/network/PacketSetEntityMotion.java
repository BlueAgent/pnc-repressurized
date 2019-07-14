package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server when an immediate update is needed to a client-side entity's motion
 */
public class PacketSetEntityMotion extends LocationDoublePacket {
    private int entityId;

    public PacketSetEntityMotion() {
    }

    public PacketSetEntityMotion(Entity entity, Vec3d motion) {
        super(motion);
        entityId = entity.getEntityId();
    }

    PacketSetEntityMotion(PacketBuffer buffer) {
        super(buffer);
        entityId = buffer.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeInt(entityId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = PneumaticCraftRepressurized.proxy.getClientWorld().getEntityByID(entityId);
            if (entity != null) {
                entity.setMotion(x, y, z);
                entity.onGround = false;
                entity.collided = false;
                entity.collidedHorizontally = false;
                entity.collidedVertically = false;
                if (entity instanceof LivingEntity) ((LivingEntity) entity).setJumping(true);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
