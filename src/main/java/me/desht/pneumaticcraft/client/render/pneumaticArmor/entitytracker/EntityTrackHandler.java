package me.desht.pneumaticcraft.client.render.pneumaticArmor.entitytracker;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IEntityTrackEntry;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IEntityTrackEntry.EntityTrackEntry;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableEntity;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IPneumaticHelmetRegistry;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.PneumaticHelmetRegistry;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.RenderDroneAI;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.RenderTarget;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.hacking.HackableHandler;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.renderHandler.DroneDebugUpgradeHandler;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.renderHandler.EntityTrackUpgradeHandler;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.renderHandler.HackUpgradeRenderHandler;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.NBTKeys;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class EntityTrackHandler {
    private static final List<IEntityTrackEntry> trackEntries = new ArrayList<>();

    public static void registerDefaultEntries() {
        IPneumaticHelmetRegistry manager = PneumaticRegistry.getInstance().getHelmetRegistry();
        manager.registerEntityTrackEntry(EntityTrackEntryLivingBase.class);
        manager.registerEntityTrackEntry(EntityTrackEntryHackable.class);
        manager.registerEntityTrackEntry(EntityTrackEntryDrone.class);
        manager.registerEntityTrackEntry(EntityTrackEntryPressurizable.class);
        manager.registerEntityTrackEntry(EntityTrackEntryAgeable.class);
        manager.registerEntityTrackEntry(EntityTrackEntryTameable.class);
        manager.registerEntityTrackEntry(EntityTrackEntryCreeper.class);
        manager.registerEntityTrackEntry(EntityTrackEntrySlime.class);
        manager.registerEntityTrackEntry(EntityTrackEntryPlayer.class);
        manager.registerEntityTrackEntry(EntityTrackEntryMob.class);
    }

    public static void init() {
        for (Class<? extends IEntityTrackEntry> clazz : PneumaticHelmetRegistry.getInstance().entityTrackEntries) {
            try {
                trackEntries.add(clazz.newInstance());
            } catch (InstantiationException e) {
                Log.error("[Entity Tracker] Couldn't registrate " + clazz.getName() + ". Does it have a parameterless constructor?");
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                Log.error("[Entity Tracker] Couldn't registrate " + clazz.getName() + ". Is it a public class?");
                e.printStackTrace();
            }
        }
    }

    public static List<IEntityTrackEntry> getTrackersForEntity(Entity entity) {
        List<IEntityTrackEntry> trackers = new ArrayList<>();
        for (IEntityTrackEntry tracker : trackEntries) {
            if (tracker.isApplicable(entity)) {
                try {
                    IEntityTrackEntry newTracker = tracker.getClass().newInstance();
                    newTracker.isApplicable(entity);//just as an initializer.
                    trackers.add(newTracker);
                } catch (Exception e) {
                    //Shouldn't happen, as we tried it in the init().
                    e.printStackTrace();
                }
            }
        }
        return trackers;
    }

    public static class EntityTrackEntryDrone extends EntityTrackEntry {
        private RenderDroneAI droneAIRenderer;

        @Override
        public boolean isApplicable(Entity entity) {
            if (entity instanceof EntityDrone) {
                droneAIRenderer = new RenderDroneAI((EntityDrone) entity);
                return true;
            } else {
                return false;
            }
        }

        public RenderDroneAI getDroneAIRenderer() {
            return droneAIRenderer;
        }

        @Override
        public void update(Entity entity) {
            droneAIRenderer.update();//TODO render
        }

        @Override
        public void render(Entity entity, float partialTicks) {
            droneAIRenderer.render(partialTicks);
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo) {
            curInfo.add("Owner: " + ((EntityDrone) entity).playerName);
            curInfo.add("Routine: " + ((EntityDrone) entity).getLabel());
            EntityPlayer player = PneumaticCraftRepressurized.proxy.getClientPlayer();
            if (DroneDebugUpgradeHandler.enabledForPlayer(player)) {
                if (NBTUtil.getInteger(player.getItemStackFromSlot(EntityEquipmentSlot.HEAD), NBTKeys.PNEUMATIC_HELMET_DEBUGGING_DRONE) == entity.getEntityId()) {
                    curInfo.add(TextFormatting.GOLD + "Debugging this drone");
                    curInfo.add(TextFormatting.GOLD + "Press [" + Keyboard.getKeyName(KeyHandler.getInstance().keybindOpenOptions.getKeyCode()) + "] for debugger");
                } else {
                    curInfo.add(TextFormatting.GOLD + "Press [" + Keyboard.getKeyName(KeyHandler.getInstance().keybindDebuggingDrone.getKeyCode()) + "] to debug");
                }
            }
        }
    }

    public static class EntityTrackEntryPressurizable extends EntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof IPressurizable;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo) {
            curInfo.add("Pressure: " + PneumaticCraftUtils.roundNumberTo(((IPressurizable) entity).getPressure(null), 1) + " bar");
        }
    }

    public static class EntityTrackEntryLivingBase extends EntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof EntityLivingBase;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo) {
            int healthPercentage = (int) (((EntityLivingBase) entity).getHealth() / ((EntityLivingBase) entity).getMaxHealth() * 100F);
            curInfo.add("Health: " + healthPercentage + "%%");
        }
    }

    public static class EntityTrackEntrySlime extends EntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof EntitySlime;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo) {
            switch (((EntitySlime) entity).getSlimeSize()) {
                case 1:
                    curInfo.add("Size: Tiny");
                    return;
                case 2:
                    curInfo.add("Size: Small");
                    return;
                case 4:
                    curInfo.add("Size: Big");
                    return;
                default:
                    curInfo.add("Size: " + ((EntitySlime) entity).getSlimeSize());
                    return;
            }
        }
    }

    public static class EntityTrackEntryMob extends EntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof EntityMob;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo) {
            Entity target = ((EntityMob) entity).getAttackTarget();
            if (target != null) {
                curInfo.add("Target: " + target.getName());
            }
        }
    }

    public static class EntityTrackEntryAgeable extends EntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof EntityAgeable;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo) {
            int growingAge = ((EntityAgeable) entity).getGrowingAge();
            if (growingAge > 0) {
                curInfo.add("Can breed in " + PneumaticCraftUtils.convertTicksToMinutesAndSeconds(growingAge, false));
            } else if (growingAge < 0) {
                curInfo.add("Becomes adult in " + PneumaticCraftUtils.convertTicksToMinutesAndSeconds(-growingAge, false));
            } else {
                curInfo.add("This animal can be bred");
            }
        }
    }

    public static class EntityTrackEntryTameable extends EntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof EntityTameable;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo) {
            EntityLivingBase owner = ((EntityTameable) entity).getOwner();
            if (owner != null) {
                curInfo.add("Owner: " + owner.getName());
            } else {
                curInfo.add("This animal can be tamed");
            }
        }
    }

    public static class EntityTrackEntryCreeper extends EntityTrackEntry {
        private int creeperInFuseTime;

        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof EntityCreeper;
        }

        @Override
        public void update(Entity entity) {
            if (((EntityCreeper) entity).getCreeperState() == 1) {
                creeperInFuseTime++;
                if (creeperInFuseTime > 30) creeperInFuseTime = 30;
            } else {
                creeperInFuseTime--;
                if (creeperInFuseTime < 0) creeperInFuseTime = 0;
            }
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo) {
            if (creeperInFuseTime > 0) {
                if (((EntityCreeper) entity).getCreeperState() == 1) {
                    curInfo.add(TextFormatting.RED + "FUSE: " + Math.round((30 - creeperInFuseTime) / 20F * 10F) / 10F + "s !");
                } else {
                    curInfo.add(TextFormatting.DARK_GREEN + "Cooling down: " + Math.round((30 - creeperInFuseTime) / 20F * 10F) / 10F + "s !");
                }
            }
        }
    }

    public static class EntityTrackEntryPlayer extends EntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof EntityPlayer;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo) {
            EntityPlayer player = (EntityPlayer) entity;
            boolean isArmorEmpty = true;
            for (ItemStack stack : player.inventory.armorInventory) {
                if (stack != null) {
                    isArmorEmpty = false;
                    break;
                }
            }
            boolean isMainInventoryEmpty = true;
            for (ItemStack stack : player.inventory.mainInventory) {
                if (stack != null) {
                    isMainInventoryEmpty = false;
                    break;
                }
            }
            curInfo.add(TextFormatting.GRAY + "Armor:" + (isArmorEmpty ? " -" : ""));
            PneumaticCraftUtils.sortCombineItemStacksAndToString(curInfo, asItemStackArray(player.inventory.armorInventory));
            curInfo.add(TextFormatting.GRAY + "Held item:" + (isMainInventoryEmpty ? " -" : ""));
            PneumaticCraftUtils.sortCombineItemStacksAndToString(curInfo, asItemStackArray(player.inventory.mainInventory));
        }
    }

    private static ItemStack[] asItemStackArray(NonNullList<ItemStack> stacks) {
        ItemStack[] result = new ItemStack[stacks.size()];
        for (int i = 0; i < stacks.size(); i++) {
            result[i] = stacks.get(i);
        }
        return result;
    }

    public static class EntityTrackEntryHackable extends EntityTrackEntry {

        @Override
        public boolean isApplicable(Entity entity) {
            return HackUpgradeRenderHandler.enabledForPlayer(PneumaticCraftRepressurized.proxy.getClientPlayer());
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo) {
            IHackableEntity hackable = HackableHandler.getHackableForEntity(entity, PneumaticCraftRepressurized.proxy.getClientPlayer());
            if (hackable != null) {
                int hackTime = HUDHandler.instance().getSpecificRenderer(EntityTrackUpgradeHandler.class).getTargetsStream()
                        .filter(target -> target.entity == entity)
                        .findFirst()
                        .map(RenderTarget::getHackTime)
                        .orElse(0);
                if (hackTime == 0) {
                    hackable.addInfo(entity, curInfo, PneumaticCraftRepressurized.proxy.getClientPlayer());
                } else {
                    int requiredHackTime = hackable.getHackTime(entity, PneumaticCraftRepressurized.proxy.getClientPlayer());
                    int percentageComplete = hackTime * 100 / requiredHackTime;
                    if (percentageComplete < 100) {
                        curInfo.add("Hacking... (" + percentageComplete + "%%)");
                    } else if (hackTime < requiredHackTime + 20) {
                        hackable.addPostHackInfo(entity, curInfo, PneumaticCraftRepressurized.proxy.getClientPlayer());
                    } else {
                        hackable.addInfo(entity, curInfo, PneumaticCraftRepressurized.proxy.getClientPlayer());
                        if (KeyHandler.getInstance().keybindHack.getKeyCode() != 0) {
                            curInfo.add(TextFormatting.GOLD + "Press [" + Keyboard.getKeyName(KeyHandler.getInstance().keybindHack.getKeyCode()) + "] to hack");
                        }
                    }
                }
            }
        }
    }
}