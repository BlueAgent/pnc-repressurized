package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.client.util.ProgWidgetRenderer;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.config.subconfig.ProgWidgetConfig;
import me.desht.pneumaticcraft.common.core.ModRegistries;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public abstract class ProgWidget implements IProgWidget {
    private final ProgWidgetType<?> type;
    private int x, y;
    private IProgWidget[] connectedParameters;
    private IProgWidget outputStepConnection;
    private IProgWidget parent;

    public ProgWidget(ProgWidgetType<?> type) {
        this.type = type;
        if (!getParameters().isEmpty())
            connectedParameters = new IProgWidget[getParameters().size() * 2]; //times two because black- and whitelist.
    }

    public ProgWidgetType<?> getType() {
        return type;
    }

    @Override
    public ResourceLocation getTypeID() {
        return getType().getRegistryName();
    }

    @Override
    public void getTooltip(List<ITextComponent> curTooltip) {
        curTooltip.add(xlate(getTranslationKey()).applyTextStyles(TextFormatting.DARK_AQUA, TextFormatting.UNDERLINE));
        if (freeToUse()) {
            curTooltip.add(new TranslationTextComponent("gui.progWidget.comment.tooltip.freeToUse"));
        }
    }

    @Override
    public String getExtraStringInfo() {
        return null;
    }

    @Override
    public void addWarnings(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        if (this instanceof IVariableWidget) {
            Set<String> variables = new HashSet<>();
            ((IVariableWidget) this).addVariables(variables);
            for (String variable : variables) {
                if (!variable.equals("") && !variable.startsWith("#") && !variable.startsWith("$") && !isVariableSetAnywhere(widgets, variable)) {
                    curInfo.add(xlate("gui.progWidget.general.warning.variableNeverSet", variable));
                }
            }
        }
    }

    private boolean isVariableSetAnywhere(List<IProgWidget> widgets, String variable) {
        if (variable.equals("")) return true;
        for (IProgWidget widget : widgets) {
            if (widget instanceof IVariableSetWidget) {
                Set<String> variables = new HashSet<>();
                ((IVariableSetWidget) widget).addVariables(variables);
                if (variables.contains(variable)) return true;
            }
        }
        return false;
    }

    @Override
    public void addErrors(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        if (!hasStepInput() && hasStepOutput() && outputStepConnection == null) {
            curInfo.add(xlate("gui.progWidget.general.error.noPieceConnected"));
        }
    }

    @Override
    public boolean isAvailable() {
        return !ProgWidgetConfig.INSTANCE.isWidgetBlacklisted(getType());
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getWidth() {
        return 30;
    }

    @Override
    public int getHeight() {
        return !getParameters().isEmpty() ? getParameters().size() * 22 : 22;
    }

    @Override
    public void setParent(IProgWidget widget) {
        parent = widget;
    }

    @Override
    public IProgWidget getParent() {
        return parent;
    }

//    @Override
//    public void render() {
//        // FIXME this won't work for in-world rendering (drone debugging)
//        Minecraft.getInstance().getTextureManager().bindTexture(getTexture());
//        int width = getWidth() + (getParameters().isEmpty() ? 0 : 10);//(getParameters() != null && getParameters().size() > 0 ? 10 : 0);
//        int height = getHeight() + (hasStepOutput() ? 10 : 0);
//        Pair<Float,Float> maxUV = getMaxUV();
//        float u = maxUV.getLeft();
//        float v = maxUV.getRight();
//        BufferBuilder wr = Tessellator.getInstance().getBuffer();
//        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
//        wr.pos(0, 0, 0).tex(0, 0).endVertex();
//        wr.pos(0, height, 0).tex(0, v).endVertex();
//        wr.pos(width, height, 0).tex(u, v).endVertex();
//        wr.pos(width, 0, 0).tex(u, 0).endVertex();
//        Tessellator.getInstance().draw();
//    }

    @Override
    public void renderExtraInfo() {
        ProgWidgetRenderer.renderExtras(this);
    }

    @Override
    public Pair<Float,Float> getMaxUV() {
        int width = getWidth() + (getParameters().isEmpty() ? 0 : 10);
        int height = getHeight() + (hasStepOutput() ? 10 : 0);
        int textureSize = getTextureSize();
        float u = (float) width / textureSize;
        float v = (float) height / textureSize;
        return new ImmutablePair<>(u, v);
    }

    @Override
    public int getTextureSize() {
        int width = getWidth() + (getParameters().isEmpty() ? 0 : 10);
        int height = getHeight() + (hasStepOutput() ? 10 : 0);
        int maxSize = Math.max(width, height);

        int textureSize = 1;
        while (textureSize < maxSize) {
            textureSize *= 2;
        }
        return textureSize;
    }

    @Override
    public boolean hasStepOutput() {
        return hasStepInput();
    }

    @Override
    public Goal getWidgetTargetAI(IDroneBase drone, IProgWidget widget) {
        return null;
    }

    @Override
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return null;
    }

    @Override
    public void setParameter(int index, IProgWidget parm) {
        int index2 = index >= getParameters().size() ? index - getParameters().size() : index;
        if (connectedParameters != null && (parm == null || parm.getType() == getParameters().get(index2)))
            connectedParameters[index] = parm;
    }

    @Override
    public boolean canSetParameter(int index) {
        if (connectedParameters != null) {
            return hasBlacklist() || index < connectedParameters.length / 2;
        }
        return false;
    }

    protected boolean hasBlacklist() {
        return true;
    }

    @Override
    public IProgWidget[] getConnectedParameters() {
        return connectedParameters;
    }

    @Override
    public void setOutputWidget(IProgWidget widget) {
        outputStepConnection = widget;
    }

    @Override
    public IProgWidget getOutputWidget() {
        return outputStepConnection;
    }

    @Override
    public IProgWidget getOutputWidget(IDroneBase drone, List<IProgWidget> allWidgets) {
        return outputStepConnection;
    }

    @Override
    public IProgWidget copy() {
        IProgWidget copy = IProgWidget.create(getType());
        CompoundNBT tag = new CompoundNBT();
        writeToNBT(tag);
        copy.readFromNBT(tag);
        return copy;
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        tag.putString("name", getTypeID().toString());
        tag.putInt("x", x);
        tag.putInt("y", y);
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        x = tag.getInt("x");
        y = tag.getInt("y");
    }

    @Override
    public void writeToPacket(PacketBuffer buf) {
        buf.writeResourceLocation(getTypeID());
        buf.writeVarInt(x);
        buf.writeVarInt(y);
    }

    @Override
    public void readFromPacket(PacketBuffer buf) {
        // note: widget type ID is not read here (see fromPacket() static method)
        x = buf.readVarInt();
        y = buf.readVarInt();
    }

    static <T extends IProgWidget> List<T> getConnectedWidgetList(IProgWidget widget, int parameterIndex, ProgWidgetType<T> type) {
        validateType(widget, parameterIndex, type);

        IProgWidget connectingWidget = widget.getConnectedParameters()[parameterIndex];
        if (connectingWidget != null) {
            List<T> list = new ArrayList<>();
            while (connectingWidget != null) {
                list.add(type.cast(connectingWidget));  // should be safe; we checked the type above
                connectingWidget = connectingWidget.getConnectedParameters()[0];
            }
            return list;
        } else {
            return null;
        }
    }

    private static <T extends IProgWidget> void validateType(IProgWidget widget, int parameterIndex, ProgWidgetType<T> type) {
        int l = widget.getParameters().size();
        if (parameterIndex >= l) parameterIndex -= l;  // blacklist side
        if (type != widget.getParameters().get(parameterIndex)) {
            throw new IllegalArgumentException(String.format("invalid type %s for parameter %d (expected %s)",
                    type, parameterIndex, widget.getParameters().get(parameterIndex)));
        }
    }

    public static IProgWidget fromPacket(PacketBuffer buf) {
        ResourceLocation typeID = buf.readResourceLocation();
        ProgWidgetType type = ModRegistries.PROG_WIDGETS.getValue(typeID);
        if (type != null) {
            IProgWidget newWidget = IProgWidget.create(type);
            newWidget.readFromPacket(buf);
            return newWidget;
        } else {
            throw new IllegalStateException("unknown widget type found: " + typeID);
        }
    }

    @Override
    public boolean canBeRunByComputers(IDroneBase drone, IProgWidget widget) {
        return getWidgetAI(drone, widget) != null;
    }

}
