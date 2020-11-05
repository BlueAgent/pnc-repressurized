package me.desht.pneumaticcraft.client.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.ContainerSecurityStationMain;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSecurityStationAddUser;
import me.desht.pneumaticcraft.common.network.PacketUpdateTextfield;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiSecurityStationInventory extends GuiSecurityStationBase<ContainerSecurityStationMain> {
    private WidgetAnimatedStat statusStat;
    private WidgetAnimatedStat accessStat;

    private WidgetButtonExtended addUserButton;
    private Button rebootButton;
    private WidgetTextField sharedUserTextField;
    private List<WidgetButtonExtended> removeUserButtons;
    private NetworkConnectionHandler nodeHandler;

    public GuiSecurityStationInventory(ContainerSecurityStationMain container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);

        ySize = 239;
    }

    @Override
    public void init() {
        super.init();

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        statusStat = addAnimatedStat(new StringTextComponent("Security Status"), new ItemStack(ModBlocks.SECURITY_STATION.get()), 0xFFFFAA00, false);
        accessStat = addAnimatedStat(new StringTextComponent("Shared Users"), new ItemStack(Items.PLAYER_HEAD), 0xFF005500, false);

        Rectangle2d accessButtonRectangle = accessStat.getButtonScaledRectangle(145, 10, 20, 20);
        addUserButton = getButtonFromRectangle(null, accessButtonRectangle, "+", b -> {
            if (!sharedUserTextField.getText().equals(""))
                NetworkHandler.sendToServer(new PacketSecurityStationAddUser(te, sharedUserTextField.getText()));
        });

        rebootButton = new WidgetButtonExtended(xStart + 110, yStart + 20, 60, 20, "Reboot").withTag("reboot");
        sharedUserTextField = getTextFieldFromRectangle(accessStat.getButtonScaledRectangle(20, 15, 120, 10));
        sharedUserTextField.setResponder(s -> {
            te.setText(0, sharedUserTextField.getText());
            NetworkHandler.sendToServer(new PacketUpdateTextfield(te, 0));
        });
        accessStat.addSubWidget(sharedUserTextField);
        accessStat.addSubWidget(addUserButton);

        addButton(new WidgetButtonExtended(guiLeft + 108, guiTop + 103, 64, 20, xlate("pneumaticcraft.gui.securityStation.test")))
                .withTag("test");
        addButton(rebootButton);
        addButton(new WidgetButtonExtended(guiLeft + 108, guiTop + 125, 64, 20, xlate("pneumaticcraft.gui.button.showRange"), b -> te.showRangeLines()));

        updateUserRemoveButtons();

        nodeHandler = new NetworkConnectionBackground(this, te, xStart + 25, yStart + 30, 18, 0xFF2222FF);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
        super.drawGuiContainerForegroundLayer(matrixStack, x, y);
        font.drawString(matrixStack, "Network Layout", 15, 12, 4210752);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_SECURITY_STATION;
    }

    @Override
    protected PointXY getInvTextOffset() {
        return new PointXY(0, 2);
    }

    @Override
    protected PointXY getInvNameOffset() {
        return new PointXY(0, -2);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float opacity, int x, int y) {
        super.drawGuiContainerBackgroundLayer(matrixStack, opacity, x, y);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        nodeHandler.render(matrixStack);
    }

    @Override
    public void tick() {
        super.tick();
        statusStat.setText(getStatusText());
        accessStat.setTextWithoutCuttingString(getAccessText());
        String rebootButtonString;
        if (te.getRebootTime() > 0) {
            rebootButtonString = te.getRebootTime() % 100 < 50 ? "Rebooting.." : PneumaticCraftUtils.convertTicksToMinutesAndSeconds(te.getRebootTime(), false);
        } else {
            rebootButtonString = "Reboot";
        }

        rebootButton.setMessage(new StringTextComponent(rebootButtonString));

        addUserButton.visible = accessStat.isDoneExpanding();
        for (Button button : removeUserButtons) {
            button.active = accessStat.isDoneExpanding();
        }
        if (removeUserButtons.size() != te.sharedUsers.size()) {
            updateUserRemoveButtons();
        }
    }

    @Override
    protected void addProblems(List<String> text) {
        super.addProblems(text);
        if (te.getRebootTime() > 0) {
            text.add(TextFormatting.GRAY + "The Security Station doesn't provide security!");
            text.add(TextFormatting.BLACK + "The station is rebooting (" + PneumaticCraftUtils.convertTicksToMinutesAndSeconds(te.getRebootTime(), false) + ").");
        } else if (te.isHacked()) {
            text.add(TextFormatting.GRAY + "This Station has been hacked!");
            text.add(TextFormatting.BLACK + "Reboot the station.");
        }
        if (!te.hasValidNetwork()) {
            text.add(TextFormatting.GRAY + "Invalid network configuration!");
            switch (te.checkForNetworkValidity()) {
                case NO_SUBROUTINE:
                    text.add(TextFormatting.BLACK + "Add a Diagnostic Subroutine.");
                    break;
                case NO_IO_PORT:
                    text.add(TextFormatting.BLACK + "Add a Network IO Port.");
                    break;
                case NO_REGISTRY:
                    text.add(TextFormatting.BLACK + "Add a Network Registry.");
                    break;
                case TOO_MANY_SUBROUTINES:
                    text.add(TextFormatting.BLACK + "There can only be one Diagnostic Subroutine.");
                    break;
                case TOO_MANY_IO_PORTS:
                    text.add(TextFormatting.BLACK + "There can only be one Network IO Port.");
                    break;
                case TOO_MANY_REGISTRIES:
                    text.add(TextFormatting.BLACK + "There can only be one Network Registry.");
                    break;
                case NO_CONNECTION_SUB_AND_IO_PORT:
                    text.add(TextFormatting.BLACK + "The Diagnostic Subroutine and the Network IO Port need to be connected in the network.");
                    break;
                case NO_CONNECTION_IO_PORT_AND_REGISTRY:
                    text.add(TextFormatting.BLACK + "The Network Registry and the Network IO Port need to be connected in the network.");
                    break;
            }
        }
    }

    private List<String> getStatusText() {
        List<String> text = new ArrayList<>();
        text.add(TextFormatting.GRAY + "Protection");
        if (te.getRebootTime() > 0) {
            text.add(TextFormatting.DARK_RED + "No protection because of rebooting!");
        } else if (te.isHacked()) {
            text.add(TextFormatting.DARK_RED + "Hacked by:");
            for (GameProfile hacker : te.hackedUsers) {
                text.add(TextFormatting.DARK_RED + "\u2022 " + hacker.getName());
            }
        } else {
            text.add(TextFormatting.BLACK + "System secure");
        }
        text.add(TextFormatting.GRAY + "Security Level");
        text.add(TextFormatting.BLACK + "Level " + te.getSecurityLevel());
        text.add(TextFormatting.GRAY + "Intruder Detection Chance");
        text.add(TextFormatting.BLACK.toString() + te.getDetectionChance() + "%%");
        text.add(TextFormatting.GRAY + "Security Range");
        text.add(TextFormatting.BLACK.toString() + te.getSecurityRange() + "m (square)");
        return text;
    }

    private List<String> getAccessText() {
        List<String> textList = new ArrayList<>();
        textList.add("                                      ");
        textList.add("");
        for (GameProfile user : te.sharedUsers) {
            textList.add(TextFormatting.WHITE + "\u2022 " + user.getName());
        }
        return textList;
    }

    private void updateUserRemoveButtons() {
        if (removeUserButtons != null) {
            for (WidgetButtonExtended button : removeUserButtons) {
                accessStat.removeSubWidget(button);
            }
        }
        removeUserButtons = new ArrayList<>();
        for (int i = 0; i < te.sharedUsers.size(); i++) {
            Rectangle2d rect = accessStat.getButtonScaledRectangle(24, 30 + i * 10, font.getStringWidth(te.sharedUsers.get(i).getName()), 8);
            WidgetButtonExtended button = getInvisibleButtonFromRectangle("remove:" + i, rect, b -> {});
            button.setInvisibleHoverColor(0x44FF0000);
            button.setVisible(false);
            accessStat.addSubWidget(button);
            removeUserButtons.add(button);
            if (te.sharedUsers.get(i).getName().equals(minecraft.player.getGameProfile().getName())) {
                button.visible = false;
            }
        }
    }
}
