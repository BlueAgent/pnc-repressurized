package me.desht.pneumaticcraft.proxy;

import me.desht.pneumaticcraft.common.event.HackTickHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class ServerProxy implements IProxy {
    private final HackTickHandler serverHackTickHandler = new HackTickHandler();

    @Override
    public void initConfig() {
    }

    @Override
    public World getClientWorld() {
        return null;
    }

    @Override
    public EntityPlayer getClientPlayer() {
        return null;
    }

    @Override
    public void preInit() {
    }

    @Override
    public void init() {
    }

    @Override
    public void postInit() {
    }

    @Override
    public int getArmorRenderID(String armorName) {
        return 0;
    }

    @Override
    public HackTickHandler getHackTickHandler() {
        return serverHackTickHandler;
    }

    @Override
    public boolean isSneakingInGui() {
        return false;
    }

    @Override
    public void addScheduledTask(Runnable runnable, boolean serverSide) {
        FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(runnable);
    }
}
