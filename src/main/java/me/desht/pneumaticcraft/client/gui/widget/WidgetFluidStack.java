package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.function.Consumer;

public class WidgetFluidStack extends WidgetFluidFilter {
    public WidgetFluidStack(int x, int y, FluidStack stack, Consumer<WidgetFluidFilter> pressable) {
        super(x, y, stack, pressable);
    }

    public FluidStack getFluidStack() {
        return fluidStack;
    }

    public WidgetFluidFilter setFluidStack(FluidStack fluidStack) {
        this.fluidStack = fluidStack;
        return this;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTick) {
        super.renderButton(mouseX, mouseY, partialTick);

        if (!fluidStack.isEmpty()) {
            int fluidAmount = fluidStack.getAmount() / 1000;
            if (fluidAmount > 1) {
                FontRenderer fr = Minecraft.getInstance().fontRenderer;
                GlStateManager.translated(0, 0, 400);  // ensure amount is drawn in front of the fluid texture
                String s = fluidAmount + "B";
                fr.drawStringWithShadow(s, x - fr.getStringWidth(s) + 17, y + 9, 0xFFFFFFFF);
                GlStateManager.translated(0, 0, -400);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.clicked(mouseX, mouseY)) {
            if (!fluidStack.isEmpty()) {
                boolean shift = Screen.hasShiftDown();
                switch (button) {
                    case 0:  // left-click: drain 1000mB (or halve with Shift held)
                        fluidStack.setAmount(shift ? fluidStack.getAmount() / 2 : Math.max(0, fluidStack.getAmount() - 1000));
                        if (fluidStack.getAmount() < 1000) fluidStack.setAmount(0);
                        break;
                    case 1:  // right-click: add 1000mB (or double with Shift held)
                        fluidStack.setAmount(shift ? fluidStack.getAmount() * 2 : fluidStack.getAmount() + 1000);
                        break;
                    case 2:  // middle-click: clear slot
                        fluidStack.setAmount(0);
                        break;

                }
            }
            if (pressable != null) pressable.accept(this);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void addTooltip(double mouseX, double mouseY, List<String> curTip, boolean shiftPressed) {
        super.addTooltip(mouseX, mouseY, curTip, shiftPressed);
        if (!fluidStack.isEmpty()) curTip.add(TextFormatting.GRAY + "" + fluidStack.getAmount() + "mB");
    }
}
