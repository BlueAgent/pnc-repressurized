package me.desht.pneumaticcraft.common;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableBlock;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableEntity;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.UpgradeRenderHandlerList;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.hacking.HackableHandler;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketHackingBlockFinish;
import me.desht.pneumaticcraft.common.network.PacketHackingEntityFinish;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.common.util.WorldAndCoord;
import me.desht.pneumaticcraft.lib.Sounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;

public class CommonHUDHandler {
    private final HashMap<String, CommonHUDHandler> playerHudHandlers = new HashMap<String, CommonHUDHandler>();
    public int rangeUpgradesInstalled;
    public int speedUpgradesInstalled;
    public boolean[] upgradeRenderersInserted = new boolean[UpgradeRenderHandlerList.instance().upgradeRenderers.size()];
    public boolean[] upgradeRenderersEnabled = new boolean[UpgradeRenderHandlerList.instance().upgradeRenderers.size()];
    public int ticksExisted;
    public float helmetPressure;

    private int hackTime;
    private WorldAndCoord hackedBlock;
    private Entity hackedEntity;

    public static CommonHUDHandler getHandlerForPlayer(EntityPlayer player) {
        CommonHUDHandler handler = PneumaticCraftRepressurized.proxy.getCommonHudHandler().playerHudHandlers.get(player.getName());
        if (handler != null) return handler;
        PneumaticCraftRepressurized.proxy.getCommonHudHandler().playerHudHandlers.put(player.getName(), new CommonHUDHandler());
        return getHandlerForPlayer(player);
    }

    @SideOnly(Side.CLIENT)
    public static CommonHUDHandler getHandlerForPlayer() {
        return getHandlerForPlayer(FMLClientHandler.instance().getClient().player);
    }

    @SubscribeEvent
    public void tickEnd(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            EntityPlayer player = event.player;
            if (this == PneumaticCraftRepressurized.proxy.getCommonHudHandler()) {
                getHandlerForPlayer(player).tickEnd(event);
            } else {
                ItemStack helmetStack = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
                if (helmetStack.getItem() == Itemss.PNEUMATIC_HELMET) {
                    helmetPressure = ((IPressurizable) helmetStack.getItem()).getPressure(helmetStack);
                    if (ticksExisted == 0) {
                        checkHelmetInventory(helmetStack);
                    }
                    ticksExisted++;
                    if (!player.world.isRemote) {
                        if (ticksExisted > getStartupTime() && !player.capabilities.isCreativeMode) {
                            float oldPressure = ((IPressurizable) helmetStack.getItem()).getPressure(helmetStack);
                            ((IPressurizable) helmetStack.getItem()).addAir(helmetStack, (int) -UpgradeRenderHandlerList.instance().getAirUsage(player, false));
                            if (oldPressure > 0F && ((IPressurizable) helmetStack.getItem()).getPressure(helmetStack) == 0F) {
                                NetworkHandler.sendTo(new PacketPlaySound(Sounds.MINIGUN_STOP, SoundCategory.PLAYERS, player.posX, player.posY, player.posZ, 1.0f, 2.0f, false), (EntityPlayerMP) player);
                            }
                        }
                    }

                } else {
                    ticksExisted = 0;
                }
                if (!player.world.isRemote) handleHacking(player);
            }
        }
    }

    private void handleHacking(EntityPlayer player) {
        if (hackedBlock != null) {
            IHackableBlock hackableBlock = HackableHandler.getHackableForCoord(hackedBlock, player);
            if (hackableBlock != null) {
                if (++hackTime >= hackableBlock.getHackTime(hackedBlock.world, hackedBlock.pos, player)) {
                    hackableBlock.onHackFinished(player.world, hackedBlock.pos, player);
                    PneumaticCraftRepressurized.proxy.getHackTickHandler().trackBlock(hackedBlock, hackableBlock);
                    NetworkHandler.sendToAllAround(new PacketHackingBlockFinish(hackedBlock), player.world);
                    setHackedBlock(null);
                }
            } else {
                setHackedBlock(null);
            }
        } else if (hackedEntity != null) {
            IHackableEntity hackableEntity = HackableHandler.getHackableForEntity(hackedEntity, player);
            if (hackableEntity != null) {
                if (++hackTime >= hackableEntity.getHackTime(hackedEntity, player)) {
                    hackableEntity.onHackFinished(hackedEntity, player);
                    PneumaticCraftRepressurized.proxy.getHackTickHandler().trackEntity(hackedEntity, hackableEntity);
                    NetworkHandler.sendToAllAround(new PacketHackingEntityFinish(hackedEntity), new NetworkRegistry.TargetPoint(hackedEntity.world.provider.getDimension(), hackedEntity.posX, hackedEntity.posY, hackedEntity.posZ, 64));
                    setHackedEntity(null);
                }
            } else {
                setHackedEntity(null);
            }
        }
    }

    public void checkHelmetInventory(ItemStack helmetStack) {
        ItemStack[] helmetStacks = UpgradableItemUtils.getUpgradeStacks(helmetStack);
        rangeUpgradesInstalled = UpgradableItemUtils.getUpgrades(EnumUpgrade.RANGE, helmetStack);
        speedUpgradesInstalled = UpgradableItemUtils.getUpgrades(EnumUpgrade.SPEED, helmetStack);
        upgradeRenderersInserted = new boolean[UpgradeRenderHandlerList.instance().upgradeRenderers.size()];
        for (int i = 0; i < UpgradeRenderHandlerList.instance().upgradeRenderers.size(); i++) {
            upgradeRenderersInserted[i] = isModuleEnabled(helmetStacks, UpgradeRenderHandlerList.instance().upgradeRenderers.get(i));
        }
    }

    private boolean isModuleEnabled(ItemStack[] helmetStacks, IUpgradeRenderHandler handler) {
        for (Item requiredUpgrade : handler.getRequiredUpgrades()) {
            boolean found = false;
            for (ItemStack stack : helmetStacks) {
                if (stack.getItem() == requiredUpgrade) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    public int getSpeedFromUpgrades() {
        return 1 + speedUpgradesInstalled;
    }

    public int getStartupTime() {
        return 200 / getSpeedFromUpgrades();
    }

    public void setHackedBlock(WorldAndCoord blockPos) {
        hackedBlock = blockPos;
        hackedEntity = null;
        hackTime = 0;
    }

    public void setHackedEntity(Entity entity) {
        hackedEntity = entity;
        hackedBlock = null;
        hackTime = 0;
    }
}
