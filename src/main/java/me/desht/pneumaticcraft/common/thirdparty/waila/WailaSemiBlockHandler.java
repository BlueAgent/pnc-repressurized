package me.desht.pneumaticcraft.common.thirdparty.waila;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockBasic;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
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

    @SuppressWarnings({"rawtypes"})
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        
        List<SemiBlockBasic> semiBlocks = SemiBlockManager.getInstance(accessor.getWorld()).getSemiBlocksAsList(SemiBlockBasic.class, accessor.getWorld(), accessor.getPosition());
        ListNBT tagList = accessor.getNBTData().getTagList("semiBlocks", Constants.NBT.TAG_COMPOUND);
        for(int i = 0; i < semiBlocks.size(); i++){
            NonNullList<ItemStack> l = NonNullList.create();
            semiBlocks.get(i).addDrops(l);
            if (!l.isEmpty()) {
                currenttip.add(TextFormatting.YELLOW + l.get(0).getDisplayName());
            }
            semiBlocks.get(i).addTooltip(currenttip, tagList.getCompoundTagAt(i), accessor.getPlayer().isSneaking());
        }
        return currenttip;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public CompoundNBT getNBTData(ServerPlayerEntity player, TileEntity te, CompoundNBT tag, World world, BlockPos pos) {
        @SuppressWarnings("rawtypes")
        List<SemiBlockBasic> semiBlocks = SemiBlockManager.getInstance(world).getSemiBlocksAsList(SemiBlockBasic.class, world, pos);
        ListNBT tagList = new ListNBT();
        tag.setTag("semiBlocks", tagList);
        for(SemiBlockBasic<?> semiBlock : semiBlocks){
            CompoundNBT subTag = new CompoundNBT();
            semiBlock.addWailaInfoToTag(subTag);
            tagList.appendTag(subTag);
        }
        return tag;
    }
}
