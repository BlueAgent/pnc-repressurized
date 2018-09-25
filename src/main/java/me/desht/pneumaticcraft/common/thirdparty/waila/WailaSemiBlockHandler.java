package me.desht.pneumaticcraft.common.thirdparty.waila;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockBasic;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.List;

public class WailaSemiBlockHandler implements IWailaDataProvider {
    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return ItemStack.EMPTY;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        
        List<SemiBlockBasic> semiBlocks = SemiBlockManager.getInstance(accessor.getWorld()).getSemiBlocksAsList(SemiBlockBasic.class, accessor.getWorld(), accessor.getPosition());
        NBTTagList tagList = accessor.getNBTData().getTagList("semiBlocks", Constants.NBT.TAG_COMPOUND);
        for(int i = 0; i < semiBlocks.size(); i++){
            semiBlocks.get(i).addWailaTooltip(currenttip, tagList.getCompoundTagAt(i), accessor.getPlayer().isSneaking());
        }
        return currenttip;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        @SuppressWarnings("rawtypes")
        List<SemiBlockBasic> semiBlocks = SemiBlockManager.getInstance(world).getSemiBlocksAsList(SemiBlockBasic.class, world, pos);
        NBTTagList tagList = new NBTTagList();
        tag.setTag("semiBlocks", tagList);
        for(SemiBlockBasic<?> semiBlock : semiBlocks){
            NBTTagCompound subTag = new NBTTagCompound();
            semiBlock.addWailaInfoToTag(subTag);
            tagList.appendTag(subTag);
        }
        return tag;
    }
}
