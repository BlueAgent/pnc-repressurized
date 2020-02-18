package me.desht.pneumaticcraft.common.util;

import com.google.common.collect.Sets;
import me.desht.pneumaticcraft.api.item.IInventoryItem;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.CoordTrackUpgradeHandler;
import me.desht.pneumaticcraft.common.item.ItemRegistry;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import me.desht.pneumaticcraft.lib.GuiConstants;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PneumaticCraftUtils {
    private static final List<Item> inventoryItemBlacklist = new ArrayList<>();

    public static ITextComponent bullet() {
        return new StringTextComponent(GuiConstants.BULLET + " ");
    }

    // this may return to Direction.HORIZONTALS one day (like in 1.12.2) but for now...
    public static final Direction[] HORIZONTALS = new Direction[] {
            Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
    };

    /**
     * Returns the EnumFacing of the given entity.
     *
     * @param entity the entity
     * @param includeUpAndDown false when UP/DOWN should not be included.
     * @return the entity's facing direction
     */
    public static Direction getDirectionFacing(LivingEntity entity, boolean includeUpAndDown) {
        double yaw = entity.rotationYaw;
        while (yaw < 0)
            yaw += 360;
        yaw = yaw % 360;
        if (includeUpAndDown) {
            if (entity.rotationPitch > 45) return Direction.DOWN;
            else if (entity.rotationPitch < -45) return Direction.UP;
        }
        if (yaw < 45) return Direction.SOUTH;
        else if (yaw < 135) return Direction.WEST;
        else if (yaw < 225) return Direction.NORTH;
        else if (yaw < 315) return Direction.EAST;
        else return Direction.SOUTH;
    }

    /**
     * Get a yaw angle from an EnumFacing
     *
     * @param facing the facing direction
     * @return the yaw angle
     */
    public static int getYawFromFacing(Direction facing) {
        switch (facing) {
            case NORTH:
                return 180;
            case SOUTH:
                return 0;
            case WEST:
                return 90;
            case EAST:
                return -90;
            default:
                return 0;
        }
    }

    public static final double[] sin;
    public static final double[] cos;
    public static final int CIRCLE_POINTS = 500;

    /*
     * Initializes the sin,cos and tan variables, so that they can be used without having to calculate them every time (render tick).
     */
    static {
        sin = new double[CIRCLE_POINTS];
        cos = new double[CIRCLE_POINTS];

        for (int i = 0; i < CIRCLE_POINTS; i++) {
            double angle = 2 * Math.PI * i / CIRCLE_POINTS;
            sin[i] = Math.sin(angle);
            cos[i] = Math.cos(angle);
        }
    }

    public static List<String> splitString(String text) {
        return splitString(text, GuiConstants.MAX_CHAR_PER_LINE_LEFT);
    }

    /**
     * This method takes one long string, and cuts it into lines which have
     * a maxCharPerLine and returns it in a String list.
     * It also preserves color formats. '\n' can be used to force a carriage
     * return.
     */
    public static List<String> splitString(String text, int maxCharPerLine) {
        StringTokenizer tok = new StringTokenizer(text, " ");
        StringBuilder output = new StringBuilder(text.length());
        List<String> textList = new ArrayList<>();
        String color = "";
        int lineLen = 0;
        while (tok.hasMoreTokens()) {
            String word = tok.nextToken();
            if (word.contains("\u00a7")) { // if there is a text formatter present.
                for (int i = 0; i < word.length() - 1; i++) {
                    if (word.substring(i, i + 2).contains("\u00a7"))
                        color = word.substring(i, i + 2); // retrieve the color format
                }
                lineLen -= 2;// don't count a color formatter with the line length.
            }
            if (lineLen + word.length() > maxCharPerLine || word.contains("\\n")) {
                word = word.replace("\\n", "");
                textList.add(output.toString());
                output.delete(0, output.length());
                output.append(color);
                lineLen = 0;
            } else if (lineLen > 0) {
                output.append(" ");
                lineLen++;
            }
            output.append(word);
            lineLen += word.length();
        }
        textList.add(output.toString());
        return textList;
    }

    public static List<ITextComponent> asStringComponent(List<String> l) {
        return l.stream().map(StringTextComponent::new).collect(Collectors.toList());
    }

    /**
     * Takes in the amount of ticks, and converts it into a time notation. 40 ticks will become "2s", while 2400 will result in "2m".
     *
     * @param ticks number of ticks
     * @param fraction When true, 30 ticks will show as '1.5s' instead of '1s'.
     * @return a formatted time
     */
    public static String convertTicksToMinutesAndSeconds(long ticks, boolean fraction) {
        String part = ticks % 20 * 5 + "";
        if (part.length() < 2) part = "0" + part;
        ticks /= 20;// first convert to seconds.
        if (ticks < 60) {
            return ticks + (fraction ? "." + part : "") + "s";
        } else {
            return ticks / 60 + "m " + ticks % 60 + "s";
        }
    }

    /**
     * Takes in any integer, and converts it into a string with a additional postfix if needed. 2300 will convert into 2k for instance.
     *
     * @param amount an integer quantity
     * @return a formatted string representation
     */
    public static String convertAmountToString(int amount) {
        if (amount < 1000) {
            return amount + "";
        } else {
            return amount / 1000 + "k";
        }
    }

    /**
     * Rounds numbers down at the given decimal. 1.234 with decimal 1 will result in a string holding "1.2"
     *
     * @param value a double-precision quantity
     * @param decimals number of digits to the right of the decimal point
     * @return a formatted string representation
     */
    public static String roundNumberTo(double value, int decimals) {
        return new BigDecimal(value).setScale(decimals, BigDecimal.ROUND_HALF_DOWN).toPlainString();
    }

    /**
     * Rounds numbers down at the given decimal. 1.234 with decimal 1 will result in a string holding "1.2"
     *
     * @param value a double-precision quantity
     * @param decimals number of digits to the right of the decimal point
     * @return the rounded value as a double-precision quantity
     */
    public static double roundNumberToDouble(double value, int decimals) {
        return new BigDecimal(value).setScale(decimals, BigDecimal.ROUND_HALF_DOWN).doubleValue();
    }

    /**
     * Rounds numbers down at the given decimal. 1.234 with decimal 1 will result in a string holding "1.2"
     *
     * @param value a floating point quantity
     * @param decimals number of digits to the right of the decimal point
     * @return a formatted string representation
     */
    public static String roundNumberTo(float value, int decimals) {
        return "" + Math.round(value * Math.pow(10, decimals)) / Math.pow(10, decimals);
    }

    /**
     * used to compare two floats which are tested for having (almost) the same value
     */
    public static boolean areFloatsEqual(float f1, float f2) {
        return areFloatsEqual(f1, f2, 0.001F);
    }

    public static boolean areFloatsEqual(float f1, float f2, float maxDifference) {
        return Math.abs(f1 - f2) < maxDifference;
    }

    private static void quickSort(ItemStack[] stacks, int begin, int end) {
        if (begin < end) {
            int partitionIndex = _partition(stacks, begin, end);

            quickSort(stacks, begin, partitionIndex-1);
            quickSort(stacks, partitionIndex+1, end);
        }
    }

    private static int _partition(ItemStack[] arr, int begin, int end) {
        ItemStack pivot = arr[end];
        int i = begin - 1;

        for (int j = begin; j < end; j++) {
            if (arr[j].getDisplayName().toString().compareToIgnoreCase(pivot.getDisplayName().toString()) <= 0) {
                i++;

                ItemStack swapTemp = arr[i];
                arr[i] = arr[j];
                arr[j] = swapTemp;
            }
        }

        ItemStack swapTemp = arr[i + 1];
        arr[i + 1] = arr[end];
        arr[end] = swapTemp;

        return i + 1;
    }

    /**
     * Sorts the stacks given alphabetically, combines them (so 2x64 will become 1x128), and adds the strings into the
     * given string list.  This method is aware of inventory items implementing the {@link IInventoryItem} interface.
     *
     * @param textList string list to add information to
     * @param originalStacks array of item stacks to sort & combine
     */
    public static void sortCombineItemStacksAndToString(List<ITextComponent> textList, ItemStack[] originalStacks) {
        sortCombineItemStacksAndToString(textList, originalStacks, "\u2022 ");
    }

    /**
     * Sorts the stacks given alphabetically, combines them (so 2x64 will become 1x128), and adds the strings into the
     * given string list.  This method is aware of inventory items implementing the {@link IInventoryItem} interface.
     *
     * @param textList string list to add information to
     * @param originalStacks array of item stacks to sort & combine
     * @param prefix prefix string to prepend to each line of output
     */
    public static void sortCombineItemStacksAndToString(List<ITextComponent> textList, ItemStack[] originalStacks, String prefix) {
        ItemStack[] stacks = Arrays.copyOf(originalStacks, originalStacks.length);
        quickSort(stacks, 0, stacks.length - 1);

        int itemCount = 0;
        ItemStack prevItemStack = ItemStack.EMPTY;
        List<ItemStack> prevInventoryItems = null;
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                if (!stack.isItemEqual(prevItemStack) || prevInventoryItems != null && prevInventoryItems.size() > 0) {
                    if (!prevItemStack.isEmpty()) {
                        addText(textList, prefix  + PneumaticCraftUtils.convertAmountToString(itemCount) + " x " + prevItemStack.getDisplayName().getFormattedText());
                    }
                    if (prevInventoryItems != null) {
                        sortCombineItemStacksAndToString(textList, prevInventoryItems.toArray(new ItemStack[0]), "\u21b3 ");
                    }
                    prevItemStack = stack;
                    itemCount = stack.getCount();
                } else {
                    itemCount += stack.getCount();
                }
                prevInventoryItems = getStacksInItem(stack);
            }
        }
        if (itemCount > 0 && !prevItemStack.isEmpty()) {
            addText(textList,prefix + PneumaticCraftUtils.convertAmountToString(itemCount) + " x " + prevItemStack.getDisplayName().getFormattedText());
            if (prevInventoryItems != null) {
                sortCombineItemStacksAndToString(textList, prevInventoryItems.toArray(new ItemStack[0]), "\u21b3 ");
            }
        }
    }

    /**
     * Get a list of the items contained in the given item.  This uses the {@link IInventoryItem} interface.
     *
     * @param item the item to check
     * @return a list of the items contained within the given item
     */
    public static List<ItemStack> getStacksInItem(@Nonnull ItemStack item) {
        List<ItemStack> items = new ArrayList<>();
        if (item.getItem() instanceof IInventoryItem && !inventoryItemBlacklist.contains(item.getItem())) {
            try {
                ((IInventoryItem) item.getItem()).getStacksInItem(item, items);
            } catch (Throwable e) {
                Log.error("An InventoryItem crashed:");
                e.printStackTrace();
                inventoryItemBlacklist.add(item.getItem());
            }
        } else {
            Iterator<IInventoryItem> iterator = ItemRegistry.getInstance().inventoryItems.iterator();
            while (iterator.hasNext()) {
                try {
                    iterator.next().getStacksInItem(item, items);
                } catch (Throwable e) {
                    Log.error("An InventoryItem crashed:");
                    e.printStackTrace();
                    iterator.remove();
                }
            }
        }
        return items;
    }

    /**
     * Returns the redstone level at the given position. Use this when you don't care what side(s) the signal is
     * coming from, just the level of the signal at the position.
     *
     * @param world the world
     * @param pos the position to check
     * @return the redstone level
     */
    public static int getRedstoneLevel(World world, BlockPos pos) {
        return world != null ? world.getRedstonePowerFromNeighbors(pos) : 0;
    }

    /**
     * Retrieve a web page from the given URL.
     *
     * @param urlString the URL
     * @return the web page
     * @throws IOException if there are any problems
     */
    public static String getPage(final String urlString) throws IOException {
        StringBuilder all = new StringBuilder();
        URL myUrl = new URL(urlString);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(myUrl.openStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                all.append(line).append(System.getProperty("line.separator"));
            }
        }

        return all.toString();
    }

    /**
     * Get a count of the number of security stations protecting the given blockpos from the given player.
     *
     * @param world the world
     * @param pos the blockpos whose protection is being checked
     * @param player the player who is being protected from
     * @param showRangeLines whether to display the stations' range bounding boxes
     * @param placementRange true when trying to place a block, false when trying to interact with a block
     * @return the number of security stations preventing access
     */
    public static int getProtectingSecurityStations(World world, BlockPos pos, PlayerEntity player, boolean showRangeLines, boolean placementRange) {
        int blockingStations = 0;
        Iterator<TileEntitySecurityStation> iterator = getSecurityStations(world, pos, placementRange).iterator();
        for (TileEntitySecurityStation station; iterator.hasNext();) {
            station = iterator.next();
            if (!station.doesAllowPlayer(player)) {
                blockingStations++;
                if (showRangeLines) station.showRangeLines();
            }
        }
        return blockingStations;
    }

    public static Stream<TileEntitySecurityStation> getSecurityStations(final World world, final BlockPos pos, final boolean placementRange) {
        return GlobalTileEntityCacheManager.getInstance().securityStations
                                                         .stream()
                                                         .filter(station -> isValidAndInRange(pos, placementRange, station));     
    }
    
    private static boolean isValidAndInRange(BlockPos pos, boolean placementRange, TileEntitySecurityStation station){
        if (station.hasValidNetwork()) {
            AxisAlignedBB aabb = station.getAffectedBoundingBox();
            if(placementRange) aabb = aabb.grow(16);
            return aabb.contains(new Vec3d(pos));
        }
        return false;
    }

    public static RayTraceResult getEntityLookedObject(LivingEntity entity) {
        return getEntityLookedObject(entity, 4.5F);
    }

    public static RayTraceResult getEntityLookedObject(LivingEntity entity, float maxDistance) {
        Pair<Vec3d, Vec3d> vecs = getStartAndEndLookVec(entity, maxDistance);
        RayTraceContext ctx = new RayTraceContext(vecs.getLeft(), vecs.getRight(), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, entity);
        return entity.world.rayTraceBlocks(ctx);
    }

    public static Pair<Vec3d, Vec3d> getStartAndEndLookVec(LivingEntity entity) {
        return getStartAndEndLookVec(entity, 4.5F);
    }

    public static Pair<Vec3d, Vec3d> getStartAndEndLookVec(LivingEntity entity, float maxDistance) {
        Vec3d entityVec;
        if (entity.world.isRemote && entity instanceof PlayerEntity) {
            entityVec = new Vec3d(entity.posX, entity.posY + 1.6200000000000001D, entity.posZ);
        } else {
            entityVec = new Vec3d(entity.posX, entity.posY + entity.getEyeHeight() - (entity.isSneaking() ? 0.08 : 0), entity.posZ);
        }
        Vec3d entityLookVec = entity.getLook(1.0F);
        Vec3d maxDistVec = entityVec.add(entityLookVec.scale(maxDistance));
        return new ImmutablePair<>(entityVec, maxDistVec);
    }

    public static BlockPos getEntityLookedBlock(LivingEntity entity, float maxDistance) {
        RayTraceResult hit = getEntityLookedObject(entity, maxDistance);
        if (hit.getType() != RayTraceResult.Type.BLOCK) {
            return null;
        }
        return ((BlockRayTraceResult) hit).getPos();
    }

    public static double distBetween(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Math.sqrt(distBetweenSq(x1, y1, z1, x2, y2, z2));
    }

    public static double distBetweenSq(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2) + Math.pow(z1 - z2, 2);
    }
    
    public static double distBetweenSq(Vec3i pos, double x, double y, double z) {
        return distBetweenSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, x, y, z);
    }

    public static double distBetweenSq(BlockPos pos1, BlockPos pos2) {
        return distBetweenSq(pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX(), pos2.getY(), pos2.getZ());
    }

    public static double distBetween(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    public static double distBetweenSq(double x1, double y1, double x2, double y2) {
        return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
    }

    public static double distBetween(Vec3i pos, double x, double y, double z) {
        return distBetween(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, x, y, z);
    }

    public static double distBetween(Vec3i pos1, Vec3i pos2) {
        return distBetween(pos1, pos2.getX() + 0.5, pos2.getY() + 0.5, pos2.getZ() + 0.5);
    }

    public static double distBetween(Vec3d vec, double x, double y, double z) {
        return distBetween(vec.x, vec.y, vec.z, x, y, z);
    }

    public static double distBetween(Vec3d vec1, Vec3d vec2) {
        return distBetween(vec1, vec2.x, vec2.y, vec2.z);
    }

    public static boolean areStacksEquivalent(@Nonnull ItemStack stack1, @Nonnull ItemStack stack2, boolean checkDurability, boolean checkNBT, boolean checkItemTags, boolean checkModSimilarity) {
        if (stack1.isEmpty() && stack2.isEmpty()) return true;
        if (stack1.isEmpty() || stack2.isEmpty()) return false;

        if (checkModSimilarity) {
            String mod1 = stack1.getItem().getRegistryName().getNamespace();
            String mod2 = stack2.getItem().getRegistryName().getNamespace();
            return mod1.equals(mod2);
        }
        if (checkItemTags) {
            return !Sets.intersection(stack1.getItem().getTags(), stack2.getItem().getTags()).isEmpty();
        }

        if (stack1.getItem() != stack2.getItem()) return false;

        boolean durabilityOK = !checkDurability || (stack1.getMaxDamage() > 0 && stack1.getDamage() == stack2.getDamage());
        boolean nbtOK = !checkNBT || (stack1.hasTag() ? stack1.getTag().equals(stack2.getTag()) : !stack2.hasTag());

        return durabilityOK && nbtOK;
    }

    public static boolean isBlockLiquid(Block block) {
        return block instanceof FlowingFluidBlock;
    }

    public static String getOrientationName(Direction dir) {
        switch (dir) {
            case UP:
                return "Top";
            case DOWN:
                return "Bottom";
            case NORTH:
                return "North";
            case SOUTH:
                return "South";
            case EAST:
                return "East";
            case WEST:
                return "West";
            default:
                return "Unknown";
        }
    }

    public static void dropItemOnGround(ItemStack stack, World world, BlockPos pos) {
        dropItemOnGround(stack, world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    public static void dropItemOnGround(ItemStack stack, World world, double x, double y, double z) {
        float dX = world.rand.nextFloat() * 0.8F + 0.1F;
        float dY = world.rand.nextFloat() * 0.8F + 0.1F;
        float dZ = world.rand.nextFloat() * 0.8F + 0.1F;

        ItemEntity entityItem = new ItemEntity(world, x + dX, y + dY, z + dZ, stack.copy());

        if (stack.hasTag()) {
            entityItem.getItem().setTag(stack.getTag().copy());
        }

        float factor = 0.05F;
        entityItem.setMotion(world.rand.nextGaussian() * factor, world.rand.nextGaussian() * factor + 0.2, world.rand.nextGaussian() * factor);
        world.addEntity(entityItem);
        stack.setCount(0);
    }

    public static void dropItemOnGroundPrecisely(ItemStack stack, World world, double x, double y, double z) {
        ItemEntity entityItem = new ItemEntity(world, x, y, z, stack.copy());

        if (stack.hasTag()) {
            entityItem.getItem().setTag(stack.getTag().copy());
        }
        entityItem.setMotion(0, 0, 0);
        world.addEntity(entityItem);
        stack.setCount(0);
    }

    public static PlayerEntity getPlayerFromId(String uuid) {
        return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUUID(UUID.fromString(uuid));
    }

    public static PlayerEntity getPlayerFromId(UUID uuid) {
        return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUUID(uuid);
    }

    public static PlayerEntity getPlayerFromName(String name) {
        return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUsername(name);
    }

    public static boolean isPlayerOp(PlayerEntity player) {
        return player.hasPermissionLevel(2);
    }

    private static RayTraceResult raytraceEntityBlocks(LivingEntity entity, double range) {
        Pair<Vec3d, Vec3d> startAndEnd = getStartAndEndLookVec(entity, (float) range);
        RayTraceContext ctx = new RayTraceContext(startAndEnd.getLeft(), startAndEnd.getRight(), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, entity);
        return entity.world.rayTraceBlocks(ctx);
    }

    public static RayTraceResult getMouseOverServer(LivingEntity lookingEntity, double range) {
        RayTraceResult result = raytraceEntityBlocks(lookingEntity, range);
        double rangeSq = range * range;
        Pair<Vec3d, Vec3d> startAndEnd = getStartAndEndLookVec(lookingEntity, (float) range);
        Vec3d eyePos = startAndEnd.getLeft();

        if (result.getType() != RayTraceResult.Type.MISS) {
            rangeSq = result.getHitVec().squareDistanceTo(eyePos);
        }

        double rangeSq2 = rangeSq;
        Vec3d hitVec = null;
        Entity focusedEntity = null;

        Vec3d lookVec = lookingEntity.getLookVec().scale(range + 1);
        AxisAlignedBB box = lookingEntity.getBoundingBox().grow(lookVec.x, lookVec.y, lookVec.z);

        for (Entity entity : lookingEntity.world.getEntitiesInAABBexcluding(lookingEntity, box, Entity::canBeCollidedWith)) {
            AxisAlignedBB aabb = entity.getBoundingBox().grow(entity.getCollisionBorderSize());
            Optional<Vec3d> vec = aabb.rayTrace(eyePos, startAndEnd.getRight());

            if (aabb.contains(eyePos)) {
                if (rangeSq2 >= 0.0D) {
                    focusedEntity = entity;
                    hitVec = vec.orElse(eyePos);
                    rangeSq2 = 0.0D;
                }
            } else if (vec.isPresent()) {
                double rangeSq3 = eyePos.squareDistanceTo(vec.get());

                if (rangeSq3 < rangeSq2 || rangeSq2 == 0.0D) {
                    if (entity == entity.getRidingEntity() && !entity.canRiderInteract()) {
                        if (rangeSq2 == 0.0D) {
                            focusedEntity = entity;
                            hitVec = vec.get();
                        }
                    } else {
                        focusedEntity = entity;
                        hitVec = vec.get();
                        rangeSq2 = rangeSq3;
                    }
                }
            }
        }

        if (focusedEntity != null && (rangeSq2 < rangeSq || result == null)) {
            result = new EntityRayTraceResult(focusedEntity, hitVec);
        }
        return result;
    }

    public static PathFinder getPathFinder() {
        WalkNodeProcessor processor = new WalkNodeProcessor();
        processor.setCanEnterDoors(true);
        return new PathFinder(processor, CoordTrackUpgradeHandler.SEARCH_RANGE * 16);
    }

    /**
     * Attempt to place a block in the world, respecting BlockEvent.PlaceEvent results.
     *
     * @param w the world
     * @param pos the position in the world
     * @param player the player who is placing the block
     * @param face the face against which the block is placed
     * @param newState the blockstate to change the position to
     * @return true if the block could be placed, false otherwise
     */
    public static boolean tryPlaceBlock(World w, BlockPos pos, PlayerEntity player, Direction face, BlockState newState) {
        BlockSnapshot snapshot = BlockSnapshot.getBlockSnapshot(w, pos);
        if (!ForgeEventFactory.onBlockPlace(player, snapshot, face)) {
            w.setBlockState(pos, newState);
            return true;
        }
        return false;
    }

    /**
     * A little hack needed here; in 1.8 players were a subclass of EntityLiving and could be used as entities for
     * pathfinding purposes.  But now they extend EntityLivingBase, and pathfinder methods only work for subclasses of
     * EntityLiving.  So create a temporary living entity at the player's location and pathfind from that.
     *
     * @param player the player to mimic
     * @return a dummy player-sized living entity
     */
    public static MobEntity createDummyEntity(PlayerEntity player) {
        ZombieEntity dummy = new ZombieEntity(player.world) {
            @Override
            protected void registerAttributes() {
                super.registerAttributes();
                this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(CoordTrackUpgradeHandler.SEARCH_RANGE);
            }
        };
        dummy.setPosition(player.posX, player.posY, player.posZ);
        return dummy;
    }

    /**
     * Convenience method, ported from 1.8.  Consume one item from the player's inventory.
     *
     * @param inv player's inventory
     * @param item item to consume
     * @return true if an item was consumed
     */
    public static boolean consumeInventoryItem(PlayerInventory inv, Item item) {
        for (int i = 0; i < inv.mainInventory.size(); ++i) {
            if (inv.mainInventory.get(i).getItem() == item) {
                inv.mainInventory.get(i).shrink(1);
                if (inv.mainInventory.get(i).getCount() <= 0) {
                    inv.mainInventory.set(i, ItemStack.EMPTY);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Add all of the non-empty items in the given item handler to the given list.
     * @param handler the item handler
     * @param items the list
     */
    public static void collectNonEmptyItems(IItemHandler handler, NonNullList<ItemStack> items) {
        if (handler != null) {
            for (int i = 0; i < handler.getSlots(); i++) {
                if (!handler.getStackInSlot(i).isEmpty()) {
                    items.add(handler.getStackInSlot(i));
                }
            }
        }
    }

    /**
     * Convenience method, ported from 1.8.  Try to consume one item from the player's inventory.
     *
     * @param inv player's inventory
     * @param stack item to consume
     * @return true if an item was consumed
     */
    public static boolean consumeInventoryItem(PlayerInventory inv, ItemStack stack) {
        int toConsume = stack.getCount();
        for (int i = 0; i < inv.mainInventory.size(); ++i) {
            ItemStack invStack = inv.mainInventory.get(i);
            if (ItemStack.areItemsEqual(invStack, stack)) {
                int consumed = Math.min(invStack.getCount(), stack.getCount());
                invStack.shrink(consumed);
                toConsume -= consumed;
                if (toConsume <= 0) return true;
            }
        }
        return toConsume <= 0;
    }

    /**
     * Get a resource location with the domain of PneumaticCraft: Repressurized's mod ID.
     *
     * @param path the path
     * @return a mod-specific ResourceLocation for the given path
     */
    public static ResourceLocation RL(String path) {
        return new ResourceLocation(Names.MOD_ID, path);
    }

    /**
     * Get a translation string for the given key.  This has support for The One Probe which runs server-side.
     *
     * @param s the translation key
     * @return the translated string (if called server-side, a string which The One Probe will handle client-side)
     */
    public static ITextComponent xlate(String s, Object... args) {
        return new TranslationTextComponent(s, args);
    }

    public static void addText(List<ITextComponent> l, String s) {
        l.add(new StringTextComponent(s));
    }

    public static String dyeColorDesc(int c) {
        // TODO 1.14 make this better
        return TextFormatting.BOLD + DyeColor.byId(c).getTranslationKey() + TextFormatting.RESET;
    }

    public static int getBurnTime(ItemStack stack) {
        int ret = stack.getBurnTime();
        return ForgeEventFactory.getItemBurnTime(stack, ret == -1 ? AbstractFurnaceTileEntity.getBurnTimes().getOrDefault(stack.getItem(), 0) : ret);
    }

    public static Vec3d getBlockCentre(BlockPos pos) {
        return new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    public static void copyItemHandler(IItemHandler source, ItemStackHandler dest) {
        dest.setSize(source.getSlots());
        for (int i = 0; i < source.getSlots(); i++) {
            dest.setStackInSlot(i, source.getStackInSlot(i).copy());
        }
    }

    public static String posToString(BlockPos pos) {
        return String.format("%d,%d,%d", pos.getX(), pos.getY(), pos.getZ());
    }
}
