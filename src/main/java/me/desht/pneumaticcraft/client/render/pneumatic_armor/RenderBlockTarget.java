package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IBlockTrackEntry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableBlock;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.render.RenderProgressBar;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker.BlockTrackEntryList;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.BlockTrackUpgradeHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.hacking.HackableHandler;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketDescriptionPacketRequest;
import me.desht.pneumaticcraft.common.network.PacketHackingBlockStart;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraftforge.client.event.InputEvent;

import java.util.ArrayList;
import java.util.List;

public class RenderBlockTarget {

    private static final float STAT_SCALE = 0.02F;
    private final World world;
    private final BlockPos pos;
    public int ticksExisted = 0;
    public final WidgetAnimatedStat stat;
    private final PlayerEntity player;
    public List<String> textList = new ArrayList<>();
    private int hackTime;
    private final BlockTrackUpgradeHandler blockTracker;
    private TileEntity te;
    private int nEntries;

    public RenderBlockTarget(World world, PlayerEntity player, BlockPos pos, TileEntity te,
                             BlockTrackUpgradeHandler blockTracker) {
        this.world = world;
        this.player = player;
        this.pos = pos;
        this.te = te;
        this.blockTracker = blockTracker;
        String title = world.getBlockState(pos).getBlock().getTranslationKey();
        BlockState state = world.getBlockState(pos);
        ItemStack stack = state.getBlock().getPickBlock(state, Minecraft.getInstance().objectMouseOver, world, pos, player);
        if (!stack.isEmpty()) {
            title = stack.getDisplayName().getFormattedText();
        }
        stat = new WidgetAnimatedStat(null, title, WidgetAnimatedStat.StatIcon.of(stack), 20, -20, 0x4000AA00, null, false);
        stat.setMinDimensionsAndReset(0, 0);
    }

    public void setTileEntity(TileEntity te) {
        this.te = te;
    }

    /**
     * Check if this target still has any applicable entries. This should be called after calling update().
     *
     * @return true if valid, false otherwise
     */
    public boolean isTargetStillValid() {
        return nEntries > 0;
    }

    private List<IBlockTrackEntry> getApplicableEntries() {
        return BlockTrackEntryList.instance.getEntriesForCoordinate(world, pos, te);
    }

    public BlockPos getPos() {
        return pos;
    }

    public double getDistanceToEntity(Entity entity) {
        return Math.sqrt(entity.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D));
    }

    public void maybeRefreshFromServer(List<IBlockTrackEntry> applicableTrackEntries) {
        for (IBlockTrackEntry entry : applicableTrackEntries) {
            entry.getServerUpdatePositions(te).forEach(p -> NetworkHandler.sendToServer(new PacketDescriptionPacketRequest(p)));
        }
    }

    public void update() {
        if (te != null && te.isRemoved()) {
            te = null;
        }

        stat.tickWidget();

        List<IBlockTrackEntry> applicableTrackEntries = getApplicableEntries();
        nEntries = applicableTrackEntries.size();

        if (world.getGameTime() % 100 == 7) {
            maybeRefreshFromServer(applicableTrackEntries);
        }

        if (!world.isAirBlock(pos)) {
            textList = new ArrayList<>();
            if (ticksExisted > 120) {
                stat.closeWindow();
                for (IBlockTrackEntry entry : applicableTrackEntries) {
                    if (blockTracker.countBlockTrackersOfType(entry) <= entry.spamThreshold()) {
                        stat.openWindow();
                        break;
                    }
                }
                if (isPlayerLookingAtTarget()) {
                    stat.openWindow();
                    addBlockTrackInfo(textList, applicableTrackEntries);
                }
                stat.setText(textList);
            } else if (ticksExisted < -30) {
                stat.closeWindow();
                stat.setText(textList);
            }
        }

        if (hackTime > 0) {
            IHackableBlock hackableBlock = HackableHandler.getHackableForCoord(world, pos, player);
            if (hackableBlock != null) {
                hackTime++;
            } else {
                hackTime = 0;
            }
        }
    }

    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks) {
        matrixStack.push();

        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;

        matrixStack.translate(x, y, z);

        if (!world.isAirBlock(pos)) {
            renderBlockHighlight(matrixStack, buffer, world, pos, partialTicks);
        }

        RenderUtils.rotateToPlayerFacing(matrixStack);

        float targetAcquireProgress = (ticksExisted + partialTicks) / 1.2f;

        if (ticksExisted > 50 && ticksExisted <= 120) {
            RenderProgressBar.render3d(matrixStack, buffer,0D, 0.4D, 1.8D, 0.7D, 0, targetAcquireProgress, 0xD0FFFF00, 0xD000FF00);
        }

        // a bit of growing or shrinking to keep the stat on screen and/or of legible size
        float mul = getStatSizeMultiplier(MathHelper.sqrt(ClientUtils.getClientPlayer().getDistanceSq(x, y, z)));
        matrixStack.scale(STAT_SCALE * mul, STAT_SCALE * mul, STAT_SCALE * mul);

        if (!world.isAirBlock(pos)) {
            if (ticksExisted > 120) {
                if (isPlayerLookingAtTarget()) {
                    stat.render3d(matrixStack, buffer, partialTicks);
                }
            } else if (ticksExisted > 50) {
                RenderUtils.renderString3d("Acquiring Target...", 0, 0, 0xFF7F7F7F, matrixStack, buffer, false, true);
                RenderUtils.renderString3d((int)targetAcquireProgress + "%", 37, 28, 0xFF002F00, matrixStack, buffer, false, true);
            } else if (ticksExisted < -30) {
                matrixStack.scale(1.5F, 1.5F, 1.5F);
                stat.render3d(matrixStack, buffer, partialTicks);
                RenderUtils.renderString3d("Lost Target!", 0, 0, 0xFFFF0000, matrixStack, buffer, false, true);
            }
        }

        matrixStack.pop();
    }

    private float getStatSizeMultiplier(double dist) {
        if (dist < 4) {
            return Math.max(0.3f, (float) (dist / 4));
        } else if (dist < 10) {
            return 1f;
        } else {
            return (float) (dist / 10);
        }
    }

    private void renderBlockHighlight(MatrixStack matrixStack, IRenderTypeBuffer buffer, World world, BlockPos pos, float partialTicks) {
        BlockState state = world.getBlockState(pos);

        float progress = ((world.getGameTime() & 0x1f) + partialTicks) / 32f;
        float cycle = MathHelper.sin((float) (progress * Math.PI));
        VoxelShape shape = state.getShape(world, pos);
        float shrink = (shape == VoxelShapes.fullCube() ? 0.05f : 0f) + cycle / 60f;
        AxisAlignedBB aabb = shape.getBoundingBox().shrink(shrink);

        float alpha = 0.5f;
        if (blockTracker.getFocusedPos() != null) {
            alpha = blockTracker.getFocusedPos().equals(pos) ? 0.75f : 0.15f;
        }
        matrixStack.push();
        matrixStack.translate(-0.5, -0.5, -0.5);
        RenderUtils.renderFrame(matrixStack, buffer, aabb, 1/64f, 0.25f, 0.75f, 0.75f, alpha, RenderUtils.FULL_BRIGHT, true);
        matrixStack.pop();
    }

    private boolean isInitialized() {
        return ticksExisted >= 120;
    }

    private void addBlockTrackInfo(List<String> textList, List<IBlockTrackEntry> entries) {
        entries.forEach(e -> e.addInformation(world, pos, te, isPlayerLookingAtTarget() ? blockTracker.getFocusedFace() : null, textList));
    }

    private boolean isPlayerLookingAtTarget() {
        return pos.equals(blockTracker.getFocusedPos());
    }

    public void hack() {
        if (isInitialized() && isPlayerLookingAtTarget()) {
            IHackableBlock block = HackableHandler.getHackableForCoord(world, pos, player);
            if (block != null && (hackTime == 0 || hackTime > block.getHackTime(world, pos, player)))
                NetworkHandler.sendToServer(new PacketHackingBlockStart(pos));
        }
    }

    public void onHackConfirmServer() {
        hackTime = 1;
    }

    public int getHackTime() {
        return hackTime;
    }

    public boolean scroll(InputEvent.MouseScrollEvent event) {
        if (isInitialized() && isPlayerLookingAtTarget()) {
            return stat.mouseScrolled(event.getMouseX(), event.getMouseY(), event.getScrollDelta());
        }
        return false;
    }
}
