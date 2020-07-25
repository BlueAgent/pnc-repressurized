package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.render.pneumatic_armor.ArmorMessage;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Collections;
import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to get a message displayed on the Pneumatic Armor HUD
 */
public class PacketSendArmorHUDMessage {
    private ITextComponent message;
    private int duration;
    private int color;

    public PacketSendArmorHUDMessage() {
    }

    public PacketSendArmorHUDMessage(ITextComponent message, int duration) {
        this(message, duration, 0x7000FF00);
    }

    public PacketSendArmorHUDMessage(ITextComponent message, int duration, int color) {
        this.message = message;
        this.duration = duration;
        this.color = color;
    }

    PacketSendArmorHUDMessage(PacketBuffer buffer) {
        this.message = buffer.readTextComponent();
        this.duration = buffer.readInt();
        this.color = buffer.readInt();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeTextComponent(this.message);
        buf.writeInt(this.duration);
        buf.writeInt(this.color);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> HUDHandler.instance().addMessage(new ArmorMessage(message, Collections.emptyList(), duration, color)));
        ctx.get().setPacketHandled(true);
    }
}
