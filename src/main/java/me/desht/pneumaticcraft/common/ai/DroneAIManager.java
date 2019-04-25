package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.api.drone.SpecialVariableRetrievalEvent;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.item.ItemRegistry;
import me.desht.pneumaticcraft.common.progwidgets.*;
import me.desht.pneumaticcraft.common.remote.GlobalVariableManager;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * This class is derived from Minecraft's {link EntityAITasks} class. As the original class would need quite a few
 * accesstransformers or reflection calls to do what I want, I've copied most of that class in here.
 */

public class DroneAIManager implements IVariableProvider {
    /**
     * A list of EntityAITaskEntrys in EntityAITasks.
     */
    private final List<EntityAITaskEntry> taskEntries = new ArrayList<>();

    /**
     * A list of EntityAITaskEntrys that are currently being executed.
     */
    private final List<EntityAITaskEntry> executingTaskEntries = new ArrayList<>();

    /**
     * Instance of Profiler.
     */
    private final Profiler theProfiler;
    private int tickCount;
    static final int TICK_RATE = 3;

    private final IDroneBase drone;
    private List<IProgWidget> progWidgets;
    private IProgWidget curActiveWidget;
    private EntityAIBase curWidgetAI;
    private EntityAIBase curWidgetTargetAI;
    private boolean stopWhenEndReached;
    private boolean wasAIOveridden;
    private String currentLabel = "Main";//Holds the name of the last label that was jumped to.

    private Map<String, BlockPos> coordinateVariables = new HashMap<>();
    private Map<String, ItemStack> itemVariables = new HashMap<>();
    private final Stack<IProgWidget> jumpBackWidgets = new Stack<>();//Used to jump back to a for each widget.

    private static final int MAX_JUMP_STACK_SIZE = 100;

    public DroneAIManager(IDroneBase drone) {
        theProfiler = drone.world().profiler;
        this.drone = drone;
        setWidgets(drone.getProgWidgets());
    }

    public DroneAIManager(IDroneBase drone, List<IProgWidget> progWidgets) {
        theProfiler = drone.world().profiler;
        this.drone = drone;
        stopWhenEndReached = true;
        setWidgets(progWidgets);
    }

    public void dontStopWhenEndReached() {
        stopWhenEndReached = false;
    }

    public void setWidgets(List<IProgWidget> progWidgets) {
        this.progWidgets = progWidgets;
        if (progWidgets.isEmpty()) {
            setActiveWidget(null);
        } else {
            for (IProgWidget widget : progWidgets) {
                if (widget instanceof IVariableWidget) {
                    ((IVariableWidget) widget).setAIManager(this);
                }
            }
            gotoFirstWidget();
        }
    }

    public void connectVariables(DroneAIManager subAI) {
        subAI.coordinateVariables = coordinateVariables;
        subAI.itemVariables = itemVariables;
    }

    public boolean isIdling() {
        return curWidgetAI == null;
    }

    public EntityAIBase getCurrentAI() {
        return curWidgetAI;
    }

    public IDroneBase getDrone() {
        return drone;
    }

    public void writeToNBT(NBTTagCompound tag) {
        NBTTagList tagList = new NBTTagList();
        for (Map.Entry<String, BlockPos> entry : coordinateVariables.entrySet()) {
            NBTTagCompound t = new NBTTagCompound();
            t.setString("key", entry.getKey());
            t.setInteger("x", entry.getValue().getX());
            t.setInteger("y", entry.getValue().getY());
            t.setInteger("z", entry.getValue().getZ());
            tagList.appendTag(t);
        }
        tag.setTag("coords", tagList);

        GlobalVariableManager.getInstance().writeItemVars(tag, itemVariables);
    }

    public void readFromNBT(NBTTagCompound tag) {
        coordinateVariables.clear();
        NBTTagList tagList = tag.getTagList("coords", 10);
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound t = tagList.getCompoundTagAt(i);
            coordinateVariables.put(t.getString("key"), new BlockPos(t.getInteger("x"), t.getInteger("y"), t.getInteger("z")));
        }

        GlobalVariableManager.readItemVars(tag, itemVariables);
    }

    @Override
    public BlockPos getCoordinate(String varName) {
        BlockPos pos;
        if (varName.startsWith("$")) {
            SpecialVariableRetrievalEvent.CoordinateVariable.Drone event = new SpecialVariableRetrievalEvent.CoordinateVariable.Drone(drone, varName.substring(1));
            MinecraftForge.EVENT_BUS.post(event);
            pos = event.coordinate;
        } else if (varName.startsWith("#")) {
            pos = GlobalVariableManager.getInstance().getPos(varName.substring(1));
        } else {
            pos = coordinateVariables.get(varName);
        }
        return pos != null ? pos : BlockPos.ORIGIN;
    }

    public void setCoordinate(String varName, BlockPos coord) {
        if (varName.startsWith("#")) {
            GlobalVariableManager.getInstance().set(varName.substring(1), coord);
        } else if (!varName.startsWith("$")) coordinateVariables.put(varName, coord);
    }

    @Nonnull
    public ItemStack getStack(String varName) {
        ItemStack item;
        if (varName.startsWith("$")) {
            SpecialVariableRetrievalEvent.ItemVariable.Drone event = new SpecialVariableRetrievalEvent.ItemVariable.Drone(drone, varName.substring(1));
            MinecraftForge.EVENT_BUS.post(event);
            item = event.item;
        } else if (varName.startsWith("#")) {
            item = GlobalVariableManager.getInstance().getItem(varName.substring(1));
        } else {
            item = itemVariables.getOrDefault(varName, ItemStack.EMPTY);
        }
        return item;
    }

    public void setItem(String varName, @Nonnull ItemStack item) {
        if (varName.startsWith("#")) {
            GlobalVariableManager.getInstance().set(varName.substring(1), item);
        } else if (!varName.startsWith("$")) itemVariables.put(varName, item);
    }

    private void updateWidgetFlow() {
        boolean isExecuting = false;
        for (EntityAITaskEntry entry : executingTaskEntries) {
            if (curWidgetAI == entry.action) {
                isExecuting = true;
                break;
            }
        }
        if (!isExecuting && curActiveWidget != null && (curWidgetTargetAI == null || !curWidgetTargetAI.shouldExecute())) {
            IProgWidget widget = curActiveWidget.getOutputWidget(drone, progWidgets);
            if (widget != null) {
                if (curActiveWidget.getOutputWidget() != widget) {
                    if (addJumpBackWidget(curActiveWidget)) return;
                }
                setActiveWidget(widget);
            } else {
                if (stopWhenEndReached) {
                    setActiveWidget(null);
                } else {
                    gotoFirstWidget();
                }
            }
        }
        if (curActiveWidget == null && !stopWhenEndReached) {
            gotoFirstWidget();
        }
    }

    private void gotoFirstWidget() {
        setLabel("Main");
        if (!jumpBackWidgets.isEmpty()) {
            setActiveWidget(jumpBackWidgets.pop());
        } else {
            for (IProgWidget widget : progWidgets) {
                if (widget instanceof ProgWidgetStart) {
                    setActiveWidget(widget);
                    return;
                }
            }
        }
    }

    private void setActiveWidget(IProgWidget widget) {
        EntityAIBase targetAI = null;
        EntityAIBase ai = null;
        if (widget != null) {
            boolean first = widget instanceof ProgWidgetStart;
            targetAI = widget.getWidgetTargetAI(drone, widget);
            ai = widget.getWidgetAI(drone, widget);
            Set<IProgWidget> visitedWidgets = new HashSet<>();//Prevent endless loops
            while (!visitedWidgets.contains(widget) && targetAI == null && ai == null) {
                visitedWidgets.add(widget);
                IProgWidget oldWidget = widget;
                widget = widget.getOutputWidget(drone, progWidgets);
                if (widget == null) {
                    if (first) {
                        return;
                    } else {
                        if (stopWhenEndReached) {
                            setActiveWidget(null);
                        } else {
                            gotoFirstWidget();
                        }
                        return;
                    }
                } else if (oldWidget.getOutputWidget() != widget) {
                    if (addJumpBackWidget(oldWidget)) return;
                }
                targetAI = widget.getWidgetTargetAI(drone, widget);
                ai = widget.getWidgetAI(drone, widget);
            }
            drone.setActiveProgram(widget);
        } else {
            setLabel("Stopped");
        }

        curActiveWidget = widget;
        if (curWidgetAI != null) removeTask(curWidgetAI);
        if (curWidgetTargetAI != null) drone.getTargetAI().removeTask(curWidgetTargetAI);
        if (ai != null) addTask(2, ai);
        if (targetAI != null) drone.getTargetAI().addTask(2, targetAI);
        curWidgetAI = ai;
        curWidgetTargetAI = targetAI;
    }

    private boolean addJumpBackWidget(IProgWidget widget) {
        if (widget instanceof IJumpBackWidget) {
            if (jumpBackWidgets.size() >= MAX_JUMP_STACK_SIZE) {
                drone.overload("jumpStackTooLarge", MAX_JUMP_STACK_SIZE);
                jumpBackWidgets.clear();
                setActiveWidget(null);
                return true;
            } else {
                jumpBackWidgets.push(widget);
            }
        }
        return false;
    }

    public List<EntityAITaskEntry> getRunningTasks() {
        return taskEntries;
    }

    public EntityAIBase getTargetAI() {
        return curWidgetTargetAI;
    }

    /**
     * START EntityAITasks code
     */

    public void addTask(int par1, EntityAIBase par2EntityAIBase) {
        taskEntries.add(new EntityAITaskEntry(par1, par2EntityAIBase));
    }

    /**
     * removes the indicated task from the entity's AI tasks.
     */
    public void removeTask(EntityAIBase par1EntityAIBase) {
        Iterator iterator = taskEntries.iterator();

        while (iterator.hasNext()) {
            EntityAITaskEntry entityaitaskentry = (EntityAITaskEntry) iterator.next();
            EntityAIBase entityaibase1 = entityaitaskentry.action;

            if (entityaibase1 == par1EntityAIBase) {
                if (executingTaskEntries.contains(entityaitaskentry)) {
                    entityaibase1.resetTask();
                    executingTaskEntries.remove(entityaitaskentry);
                }

                iterator.remove();
            }
        }
    }
    
    private void pickupItemsIfMagnet() {
        int magnetUpgrades = drone.getUpgrades(ItemRegistry.getInstance().getUpgrade(EnumUpgrade.MAGNET));
        if (magnetUpgrades > 0) {
            int range = Math.min(6, 1 + magnetUpgrades);
            Vec3d v = drone.getDronePos();
            AxisAlignedBB aabb = new AxisAlignedBB(v.x, v.y, v.z, v.x, v.y, v.z).grow(range);
            List<EntityItem> items = drone.world().getEntitiesWithinAABB(EntityItem.class, aabb,
                    item -> item != null
                            && item.isEntityAlive()
                            && !item.cannotPickup()
                            && drone.getDronePos().squareDistanceTo(item.getPositionVector()) <= range * range);

            for (EntityItem item : items) {
                DroneEntityAIPickupItems.tryPickupItem(drone, item);
            }
        }
    }

    public void onUpdateTasks() {
        pickupItemsIfMagnet();
        
        if (ConfigHandler.advanced.stopDroneAI) return;
        if (!drone.isAIOverriden()) {
            if (wasAIOveridden && curWidgetTargetAI != null) drone.getTargetAI().addTask(2, curWidgetTargetAI);
            wasAIOveridden = false;
            ArrayList<EntityAITaskEntry> arraylist = new ArrayList<>();
            Iterator<EntityAITaskEntry> iterator;
            EntityAITaskEntry entityaitaskentry;

            if (tickCount++ % TICK_RATE == 0) {
                iterator = taskEntries.iterator();

                while (iterator.hasNext()) {
                    entityaitaskentry = iterator.next();
                    boolean flag = executingTaskEntries.contains(entityaitaskentry);

                    if (flag) {
                        if (canUse(entityaitaskentry) && canContinue(entityaitaskentry)) {
                            continue;
                        }

                        entityaitaskentry.action.resetTask();
                        executingTaskEntries.remove(entityaitaskentry);
                    }

                    if (canUse(entityaitaskentry) && entityaitaskentry.action.shouldExecute()) {
                        arraylist.add(entityaitaskentry);
                        executingTaskEntries.add(entityaitaskentry);
                    }
                }
                updateWidgetFlow();
            } else {
                iterator = executingTaskEntries.iterator();

                while (iterator.hasNext()) {
                    entityaitaskentry = iterator.next();

                    if (!entityaitaskentry.action.shouldContinueExecuting()) {
                        entityaitaskentry.action.resetTask();
                        iterator.remove();
                    }
                }
            }

            theProfiler.startSection("goalStart");
            iterator = arraylist.iterator();

            while (iterator.hasNext()) {
                entityaitaskentry = iterator.next();
                theProfiler.startSection(entityaitaskentry.action.getClass().getSimpleName());
                entityaitaskentry.action.startExecuting();
                theProfiler.endSection();
            }

            theProfiler.endSection();
            theProfiler.startSection("goalTick");
            iterator = executingTaskEntries.iterator();

            while (iterator.hasNext()) {
                entityaitaskentry = iterator.next();
                entityaitaskentry.action.updateTask();
            }

            theProfiler.endSection();
        } else {//drone charging ai is running
            if (!wasAIOveridden && curWidgetTargetAI != null) {
                drone.getTargetAI().removeTask(curWidgetTargetAI);
            }
            wasAIOveridden = true;
            for (EntityAITaskEntry ai : executingTaskEntries) {
                ai.action.resetTask();
            }
            executingTaskEntries.clear();
            drone.setDugBlock(null);
        }
    }

    /**
     * Determine if a specific AI Task should continue being executed.
     */
    private boolean canContinue(EntityAITaskEntry par1EntityAITaskEntry) {
        theProfiler.startSection("canContinue");
        boolean flag = par1EntityAITaskEntry.action.shouldContinueExecuting();
        theProfiler.endSection();
        return flag;
    }

    /**
     * Determine if a specific AI Task can be executed, which means that all running higher (= lower int value) priority
     * tasks are compatible with it or all lower priority tasks can be interrupted.
     */
    private boolean canUse(EntityAITaskEntry par1EntityAITaskEntry) {
        theProfiler.startSection("canUse");
        Iterator iterator = taskEntries.iterator();

        while (iterator.hasNext()) {
            EntityAITaskEntry entityaitaskentry1 = (EntityAITaskEntry) iterator.next();

            if (entityaitaskentry1 != par1EntityAITaskEntry) {
                if (par1EntityAITaskEntry.priority >= entityaitaskentry1.priority) {
                    if (executingTaskEntries.contains(entityaitaskentry1) && !areTasksCompatible(par1EntityAITaskEntry, entityaitaskentry1)) {
                        theProfiler.endSection();
                        return false;
                    }
                } else if (executingTaskEntries.contains(entityaitaskentry1) && !entityaitaskentry1.action.isInterruptible()) {
                    theProfiler.endSection();
                    return false;
                }
            }
        }

        theProfiler.endSection();
        return true;
    }

    /**
     * Returns whether two EntityAITaskEntries can be executed concurrently
     */
    private boolean areTasksCompatible(EntityAITaskEntry par1EntityAITaskEntry, EntityAITaskEntry par2EntityAITaskEntry) {
        return (par1EntityAITaskEntry.action.getMutexBits() & par2EntityAITaskEntry.action.getMutexBits()) == 0;
    }

    public void setLabel(String label) {
        currentLabel = label;
        drone.updateLabel();
    }

    public String getLabel() {
        if (curWidgetAI instanceof DroneAIExternalProgram) {
            return ((DroneAIExternalProgram) curWidgetAI).getRunningAI().getLabel() + " --> " + currentLabel;
        } else {
            return currentLabel;
        }
    }

    public class EntityAITaskEntry {
        /**
         * The EntityAIBase object.
         */
        public final EntityAIBase action;
        /**
         * Priority of the EntityAIBase
         */
        public final int priority;

        public EntityAITaskEntry(int par2, EntityAIBase par3EntityAIBase) {
            priority = par2;
            action = par3EntityAIBase;
        }
    }

}
