package me.desht.pneumaticcraft.client.render.area;

import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.client.pneumatic_armor.ArmorUpgradeClientRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.DroneDebugClientHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemCamoApplicator;
import me.desht.pneumaticcraft.common.item.ItemGPSAreaTool;
import me.desht.pneumaticcraft.common.tileentity.ICamouflageableTE;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public enum AreaRenderManager {
    INSTANCE;

    private static final int MAX_DISPLAYED_POS = 15000;

    private final Map<BlockPos, AreaRenderer> showHandlers = new HashMap<>();
    private World world;
    private DroneDebugClientHandler droneDebugger;

    private List<AreaRenderer> cachedPositionProviderShowers;
    private AreaRenderer camoPositionShower;
    private BlockPos lastPlayerPos;
    private int lastItemHashCode = 0;

    public static AreaRenderManager getInstance() {
        return INSTANCE;
    }

    @SubscribeEvent
    public void renderWorldLastEvent(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;

        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        MatrixStack matrixStack = event.getMatrixStack();

        matrixStack.push();

        Vector3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
        matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

        // tile entity controlled renderers
        for (AreaRenderer handler : showHandlers.values()) {
            handler.render(matrixStack, buffer);
        }

        // some special rendering for certain items
        maybeRenderPositionProvider(matrixStack, buffer, player);
        maybeRenderCamo(matrixStack, buffer, player);
        maybeRenderDroneDebug(matrixStack, buffer, player);
        maybeRenderAreaTool(matrixStack, buffer, player);

        matrixStack.pop();
    }

    @SubscribeEvent
    public void tickEnd(TickEvent.ClientTickEvent event) {
        PlayerEntity player = ClientUtils.getClientPlayer();
        if (player != null) {
            if (player.world != world) {
                world = player.world;
                showHandlers.clear();
            } else {
                if (event.phase == TickEvent.Phase.END) {
                    showHandlers.keySet().removeIf(pos -> PneumaticCraftUtils.distBetweenSq(pos, player.getPosition()) < 1024 && world.isAirBlock(pos));
                }
            }
        }
    }

    private void maybeRenderAreaTool(MatrixStack matrixStack, IRenderTypeBuffer.Impl buffer, PlayerEntity player) {
        ItemStack curItem = getHeldPositionProvider(player);
        if (curItem.getItem() instanceof ItemGPSAreaTool) {
            // show the raw P1/P2 positions; the area is shown by getHeldPositionProvider()
            BlockPos p1 = ItemGPSAreaTool.getGPSLocation(player.getEntityWorld(), curItem, 0);
            BlockPos p2 = ItemGPSAreaTool.getGPSLocation(player.getEntityWorld(), curItem, 1);
            new AreaRenderer(Collections.singleton(p1), 0x80FF6060, true).render(matrixStack, buffer);
            new AreaRenderer(Collections.singleton(p2), 0x8060FF60, true).render(matrixStack, buffer);
        }
    }

    private void maybeRenderDroneDebug(MatrixStack matrixStack, IRenderTypeBuffer.Impl buffer, PlayerEntity player) {
        ItemStack helmet = player.getItemStackFromSlot(EquipmentSlotType.HEAD);
        if (helmet.getItem() == ModItems.PNEUMATIC_HELMET.get()) {
            if (droneDebugger == null) {
                droneDebugger = ArmorUpgradeClientRegistry.getInstance().byClass(DroneDebugClientHandler.class);
            }
            Set<BlockPos> posSet = droneDebugger.getShowingPositions();
            Set<BlockPos> areaSet = droneDebugger.getShownArea();
            new AreaRenderer(posSet, 0x90FF0000, true).render(matrixStack, buffer);
            new AreaRenderer(areaSet, 0x4040FFA0, true).render(matrixStack, buffer);
        }
    }

    private ItemStack getHeldPositionProvider(PlayerEntity player) {
        if (player.getHeldItemMainhand().getItem() instanceof IPositionProvider) {
            return player.getHeldItemMainhand();
        } else if (player.getHeldItemOffhand().getItem() instanceof IPositionProvider) {
            return player.getHeldItemOffhand();
        } else {
            return ItemStack.EMPTY;
        }
    }

    private void maybeRenderPositionProvider(MatrixStack matrixStack, IRenderTypeBuffer.Impl buffer, PlayerEntity player) {
        ItemStack curItem = getHeldPositionProvider(player);
        if (curItem.getItem() instanceof IPositionProvider && curItem.hasTag()) {
            int thisHash = curItem.getTag().hashCode();
            if (thisHash != lastItemHashCode) {
                // Position data has changed: recache stored positions
                lastItemHashCode = thisHash;
                IPositionProvider positionProvider = (IPositionProvider) curItem.getItem();
                List<BlockPos> posList = positionProvider.getStoredPositions(player.getEntityWorld(), curItem);
                if (posList.size() > MAX_DISPLAYED_POS) {
                    posList.sort(Comparator.comparingDouble(blockPos -> blockPos.distanceSq(player.getPosition())));
                    player.sendStatusMessage(xlate("pneumaticcraft.message.gps_tool.culledRenderArea", posList.size()).mergeStyle(TextFormatting.GOLD), false);
                }
                Int2ObjectMap<Set<BlockPos>> colorsToPositions = new Int2ObjectOpenHashMap<>();
                int n = Math.min(posList.size(), MAX_DISPLAYED_POS);
                for (int i = 0; i < n; i++) {
                    int renderColor = positionProvider.getRenderColor(i);
                    if (posList.get(i) != null && renderColor != 0) {
                        Set<BlockPos> positionsForColor = colorsToPositions.get(renderColor);
                        if (positionsForColor == null) {
                            positionsForColor = new HashSet<>();
                            colorsToPositions.put(renderColor, positionsForColor);
                        }
                        positionsForColor.add(posList.get(i));
                    }
                }
                cachedPositionProviderShowers = new ArrayList<>(colorsToPositions.size());
                colorsToPositions.int2ObjectEntrySet().forEach((entry) ->
                        cachedPositionProviderShowers.add(new AreaRenderer(entry.getValue(), entry.getIntKey(), positionProvider.disableDepthTest())));
            }

            cachedPositionProviderShowers.forEach(renderer -> renderer.render(matrixStack, buffer));
        }
    }

    private void maybeRenderCamo(MatrixStack matrixStack, IRenderTypeBuffer.Impl buffer, PlayerEntity player) {
        if (!(player.getHeldItemMainhand().getItem() instanceof ItemCamoApplicator)) {
            return;
        }
        if (lastPlayerPos == null || camoPositionShower == null || player.getDistanceSq(lastPlayerPos.getX(), lastPlayerPos.getY(), lastPlayerPos.getZ()) > 9) {
            lastPlayerPos = player.getPosition();
            Set<BlockPos> s = Minecraft.getInstance().world.loadedTileEntityList.stream()
                    .filter(te -> te instanceof ICamouflageableTE && te.getPos().distanceSq(lastPlayerPos) < 144)
                    .map(TileEntity::getPos)
                    .collect(Collectors.toSet());
            camoPositionShower = new AreaRenderer(s, 0x408080FF, 0.75f, true, true);
        }
        if (camoPositionShower != null) {
            camoPositionShower.render(matrixStack, buffer);
        }
    }

    public AreaRenderer showArea(BlockPos[] area, int color, TileEntity areaShower) {
        return showArea(new HashSet<>(Arrays.asList(area)), color, areaShower);
    }

    public AreaRenderer showArea(Set<BlockPos> area, int color, TileEntity areaShower, boolean depth) {
        if (areaShower == null) return null;
        removeHandlers(areaShower);
        AreaRenderer handler = new AreaRenderer(area, color, depth);
        showHandlers.put(new BlockPos(areaShower.getPos().getX(), areaShower.getPos().getY(), areaShower.getPos().getZ()), handler);
        return handler;
    }

    public AreaRenderer showArea(Set<BlockPos> area, int color, TileEntity areaShower) {
        return showArea(area, color, areaShower, true);
    }

    public boolean isShowing(TileEntity te) {
        return showHandlers.containsKey(new BlockPos(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ()));
    }

    public void removeHandlers(TileEntity te) {
        showHandlers.remove(new BlockPos(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ()));
    }

    public void clearPosProviderCache() {
        // called on global variable sync, force a recalc of any cached position provider data
        lastItemHashCode = 0;
    }
}
