package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.List;
import java.util.Random;

public class HackableLivingDisarm implements IHackableEntity {
    @Override
    public String getId() {
        return null;
    }

    @Override
    public boolean canHack(Entity entity, EntityPlayer player) {
        if (entity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            IItemHandler handler = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            for (int i = 0; i < handler.getSlots(); i++) {
                if (!handler.getStackInSlot(i).isEmpty()) return true;
            }
        }
        return false;
    }

    @Override
    public void addInfo(Entity entity, List<String> curInfo, EntityPlayer player) {
        curInfo.add("pneumaticHelmet.hacking.result.disarm");
    }

    @Override
    public void addPostHackInfo(Entity entity, List<String> curInfo, EntityPlayer player) {
        curInfo.add("pneumaticHelmet.hacking.finished.disarmed");
    }

    @Override
    public int getHackTime(Entity entity, EntityPlayer player) {
        return 60;
    }

    @Override
    public void onHackFinished(Entity entity, EntityPlayer player) {
        if (!entity.world.isRemote && entity instanceof EntityLiving) {
            EntityLiving entityLiving = (EntityLiving) entity;

            // access from vertical gets hands, horizontal gets armor - see EntityLivingBase#getCapability
            IItemHandler handsHandler = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
            IItemHandler armorHandler = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.NORTH);

            doDisarm(entityLiving, player.getRNG(), handsHandler, entityLiving.inventoryHandsDropChances);
            doDisarm(entityLiving, player.getRNG(), armorHandler, entityLiving.inventoryArmorDropChances);
            entityLiving.setCanPickUpLoot(false);
        }
    }

    private void doDisarm(EntityLiving entity, Random rand, IItemHandler handler, float[] dropChances) {
        if (handler == null) return;

        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            boolean flag1 = dropChances[i] > 1.0F;
            if (!stack.isEmpty() && rand.nextFloat() < dropChances[i]) {
                if (!flag1 && stack.isItemStackDamageable()) {
                    int k = Math.max(stack.getMaxDamage() - 25, 1);
                    int l = stack.getMaxDamage() - rand.nextInt(rand.nextInt(k) + 1);
                    stack.setItemDamage(MathHelper.clamp(l, 1, k));
                }
                entity.entityDropItem(stack, 0f);
            }
            handler.extractItem(i, stack.getCount(), false);
        }
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }

}
