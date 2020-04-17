package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.*;
import me.desht.pneumaticcraft.api.hacking.IHacking;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker.BlockTrackEntryList;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.tags.NetworkTagCollection;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.Validate;

import java.util.*;

public class PneumaticHelmetRegistry implements IPneumaticHelmetRegistry {

    private static final PneumaticHelmetRegistry INSTANCE = new PneumaticHelmetRegistry();
    public final List<Class<? extends IEntityTrackEntry>> entityTrackEntries = new ArrayList<>();
    public final Map<Class<? extends Entity>, Class<? extends IHackableEntity>> hackableEntities = new HashMap<>();
    private final Map<Block, Class<? extends IHackableBlock>> hackableBlocks = new HashMap<>();
    private final Map<Block, Class<? extends IHackableBlock>> hackableTaggedBlocks = new HashMap<>();
    public final Set<Block> allHackableBlocks = new HashSet<>();
    public final Map<String, Class<? extends IHackableEntity>> stringToEntityHackables = new HashMap<>();
    public final Map<String, Class<? extends IHackableBlock>> stringToBlockHackables = new HashMap<>();
    private final Map<ResourceLocation, Class<? extends IHackableBlock>> pendingBlockTags = new HashMap<>();

    public static PneumaticHelmetRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public void registerEntityTrackEntry(Class<? extends IEntityTrackEntry> entry) {
        if (entry == null) throw new NullPointerException("Can't register null!");
        entityTrackEntries.add(entry);
    }

    @Override
    public void addHackable(Class<? extends Entity> entityClazz, Class<? extends IHackableEntity> iHackable) {
        if (entityClazz == null) throw new NullPointerException("Entity class is null!");
        if (iHackable == null) throw new NullPointerException("IHackableEntity is null!");
        if (Entity.class.isAssignableFrom(iHackable)) {
            Log.warning("Entities that implement IHackableEntity shouldn't be registered as hackable! Registering entity: " + entityClazz.getCanonicalName());
        } else {
            try {
                IHackableEntity hackableEntity = iHackable.newInstance();
                if (hackableEntity.getId() != null) stringToEntityHackables.put(hackableEntity.getId(), iHackable);
                hackableEntities.put(entityClazz, iHackable);
            } catch (InstantiationException e) {
                Log.error("Not able to register hackable entity: " + iHackable.getName() + ". Does the class have a parameterless constructor?");
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                Log.error("Not able to register hackable entity: " + iHackable.getName() + ". Is the class a public class?");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void addHackable(Block block, Class<? extends IHackableBlock> iHackable) {
        if (block == null) throw new NullPointerException("Block is null! class = " + iHackable);
        if (iHackable == null) throw new NullPointerException("IHackableBlock is null! block = " + block.getRegistryName());

        if (Block.class.isAssignableFrom(iHackable)) {
            Log.warning("Blocks that implement IHackableBlock shouldn't be registered as hackable! Registering block: " + block.getRegistryName());
        } else {
            try {
                IHackableBlock hackableBlock = iHackable.newInstance();
                if (hackableBlock.getId() != null) stringToBlockHackables.put(hackableBlock.getId(), iHackable);
                hackableBlocks.put(block, iHackable);
            } catch (InstantiationException e) {
                Log.error("Not able to register hackable block: " + iHackable.getName() + ". Does the class have a parameterless constructor?");
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                Log.error("Not able to register hackable block: " + iHackable.getName() + ". Is the class a public class?");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void addHackable(Tag<Block> blockTag, Class<? extends IHackableBlock> iHackable) {
        // can't add these yet because tags aren't populated at this point
        // we'll resolve them later (resolveBlockTags())
        pendingBlockTags.put(blockTag.getId(), iHackable);
    }

    /**
     * Called from TagsUpdatedEvent on both server and client
     */
    public void resolveBlockTags(NetworkTagCollection<Block> blocks) {
        hackableTaggedBlocks.clear();
        pendingBlockTags.forEach((id, hackable) -> blocks.get(id).getAllElements().forEach(block -> hackableTaggedBlocks.put(block, hackable)));
        allHackableBlocks.clear();
        allHackableBlocks.addAll(hackableBlocks.keySet());
        allHackableBlocks.addAll(hackableTaggedBlocks.keySet());
    }

    @Override
    public List<IHackableEntity> getCurrentEntityHacks(Entity entity) {
        return entity.getCapability(PNCCapabilities.HACKING_CAPABILITY).map(IHacking::getCurrentHacks).orElse(Collections.emptyList());
    }

    @Override
    public void registerBlockTrackEntry(IBlockTrackEntry entry) {
        BlockTrackEntryList.instance.trackList.add(entry);
    }

    @Override
    public void registerRenderHandler(IUpgradeRenderHandler renderHandler) {
        Validate.notNull(renderHandler, "Render handler can't be null!");
        UpgradeRenderHandlerList.instance().addUpgradeRenderer(renderHandler);
    }

    public Class<? extends IHackableBlock> getHackableBlock(Block block) {
        return hackableBlocks.getOrDefault(block, hackableTaggedBlocks.get(block));
    }
}
