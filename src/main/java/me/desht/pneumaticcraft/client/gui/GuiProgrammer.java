package me.desht.pneumaticcraft.client.gui;

import com.google.common.base.CaseFormat;
import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.client.gui.programmer.GuiProgWidgetOptionBase;
import me.desht.pneumaticcraft.client.gui.programmer.ProgWidgetGuiManager;
import me.desht.pneumaticcraft.client.gui.widget.GuiButtonSpecial;
import me.desht.pneumaticcraft.client.gui.widget.GuiCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.GuiRadioButton;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.inventory.ContainerProgrammer;
import me.desht.pneumaticcraft.common.item.ItemGPSAreaTool;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketGuiButton;
import me.desht.pneumaticcraft.common.network.PacketProgrammerUpdate;
import me.desht.pneumaticcraft.common.network.PacketUpdateTextfield;
import me.desht.pneumaticcraft.common.progwidgets.*;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget.WidgetDifficulty;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GuiProgrammer extends GuiPneumaticContainerBase<ContainerProgrammer,TileEntityProgrammer> {
    private GuiPastebin pastebinGui;

    private GuiButtonSpecial importButton;
    private GuiButtonSpecial exportButton;
    private GuiButtonSpecial allWidgetsButton;
    private List<GuiRadioButton> difficultyButtons;
    private GuiCheckBox showInfo, showFlow;
    private WidgetTextField nameField;
    private WidgetTextField filterField;
    private GuiButtonSpecial undoButton, redoButton;
    private GuiButtonSpecial convertToRelativeButton;

    private final List<IProgWidget> visibleSpawnWidgets = new ArrayList<>();
    private BitSet filteredSpawnWidgets;

    private GuiUnitProgrammer programmerUnit;
    private boolean wasClicking;
    private boolean wasFocused;
    private IProgWidget draggingWidget;
    private int lastMouseX, lastMouseY;
    private int dragMouseStartX, dragMouseStartY;
    private int dragWidgetStartX, dragWidgetStartY;
    private static final int FAULT_MARGIN = 4;
    private int widgetPage;
    private int maxPage;

    private boolean showingAllWidgets;
    private int showingWidgetProgress;
    private int oldShowingWidgetProgress;

    private static final Rectangle PROGRAMMER_STD_RES = new Rectangle(5, 17, 294, 154);
    private static final Rectangle PROGRAMMER_HI_RES = new Rectangle(5, 17, 644, 410);

    private static final int WIDGET_X_SPACING = 22; // x size of widgets in the widget tray

    private boolean hiRes;

    public GuiProgrammer(ContainerProgrammer container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
        
        hiRes = container.isHiRes();
        xSize = hiRes ? 700 : 350;
        ySize = hiRes ? 512 : 256;
    }

    private Rectangle getProgrammerBounds() {
        return hiRes ? PROGRAMMER_HI_RES : PROGRAMMER_STD_RES;
    }

    private int getWidgetTrayRight() {
        return hiRes ? 672 : 322;
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return hiRes ? Textures.GUI_PROGRAMMER_LARGE : Textures.GUI_PROGRAMMER_STD;
    }

    private void updateVisibleProgWidgets() {
        int y = 0, page = 0;
        int x = getWidgetTrayRight() - maxPage * WIDGET_X_SPACING;
        boolean showAllWidgets = showingWidgetProgress == WIDGET_X_SPACING * maxPage && showingAllWidgets;
        filterField.setVisible(showAllWidgets);

        maxPage = 0;
        visibleSpawnWidgets.clear();
        int difficulty = 0;
        for (int i = 0; i < difficultyButtons.size(); i++) {
            if (difficultyButtons.get(i).checked) {
                difficulty = i;
                break;
            }
        }
        List<IProgWidget> registeredWidgets = WidgetRegistrator.registeredWidgets;
        for (int i = 0; i < registeredWidgets.size(); i++) {
            IProgWidget widget = registeredWidgets.get(i);
            if (difficulty >= widget.getDifficulty().ordinal()) {
                widget.setY(y + 40);
                widget.setX(showAllWidgets ? x : getWidgetTrayRight());
                int widgetHeight = widget.getHeight() / 2 + (widget.hasStepOutput() ? 5 : 0) + 1;
                y += widgetHeight;

                if (showAllWidgets || page == widgetPage) {
                    visibleSpawnWidgets.add(widget);
                }
                if (y > ySize - (hiRes ? 260 : 160)) {
                    y = 0;
                    x += WIDGET_X_SPACING;
                    page++;
                    if (i < registeredWidgets.size() - 1) maxPage++;
                }
            }
        }
        maxPage++;

        filterField.x = Math.min(guiLeft + getWidgetTrayRight() - 25 - filterField.getWidth(), guiLeft + getWidgetTrayRight() - (maxPage * WIDGET_X_SPACING) - 2);
        filterSpawnWidgets();

        if (widgetPage >= maxPage) {
            widgetPage = maxPage - 1;
            updateVisibleProgWidgets();
        }
    }

    private void filterSpawnWidgets() {
        String filterText = filterField.getText().trim();
        if (!visibleSpawnWidgets.isEmpty() && !filterText.isEmpty()) {
            filteredSpawnWidgets = new BitSet(visibleSpawnWidgets.size());
            for (int i = 0; i < visibleSpawnWidgets.size(); i++) {
                IProgWidget widget = visibleSpawnWidgets.get(i);
                String widgetName = I18n.format("programmingPuzzle." + widget.getWidgetString() + ".name");
                filteredSpawnWidgets.set(i, widgetName.toLowerCase().contains(filterText.toLowerCase()));
            }
        } else {
            filteredSpawnWidgets = null;
        }
    }

    @Override
    protected boolean shouldAddInfoTab() {
        return false;
    }

    @Override
    public void init() {
        boolean pastebinLoaded = false;

        if (pastebinGui != null && pastebinGui.outputTag != null) {
            te.readProgWidgetsFromNBT(pastebinGui.outputTag);
            pastebinGui = null;
            NetworkHandler.sendToServer(new PacketProgrammerUpdate(te));
            pastebinLoaded = true;
        }

        super.init();

        if (programmerUnit != null) {
            te.translatedX = programmerUnit.getTranslatedX();
            te.translatedY = programmerUnit.getTranslatedY();
            te.zoomState = programmerUnit.getLastZoom();
            if (pastebinLoaded) {
                programmerUnit.gotoPiece(findWidget(te.progWidgets, ProgWidgetStart.class));
            }
        }

        Rectangle bounds = getProgrammerBounds();
        programmerUnit = new GuiUnitProgrammer(te.progWidgets, font, guiLeft, guiTop, width, height,
                bounds.x, bounds.y, bounds.width, bounds.height, te.translatedX, te.translatedY, te.zoomState);
        addButton(programmerUnit.getScrollBar());

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        // right and bottom edges of the programming area
        int xRight = getProgrammerBounds().x + getProgrammerBounds().width; // 299 or 649
        int yBottom = getProgrammerBounds().y + getProgrammerBounds().height + 3; // 171 or 427

        importButton = new GuiButtonSpecial(xStart + xRight + 2, yStart + 3, 20, 15, "\u27f5").withTag("import");
        importButton.setTooltipText("Import program");
        addButton(importButton);

        exportButton = new GuiButtonSpecial(xStart + xRight + 2, yStart + 20, 20, 15, "\u27f6").withTag("export");
        addButton(exportButton);

        addButton(new Button(xStart + xRight - 3, yStart + yBottom, 10, 10, "\u25c0",
                b -> adjustPage(-1)));
        addButton(new Button(xStart + xRight + 38, yStart + yBottom, 10, 10, "\u25b6",
                b -> adjustPage(1)));

        allWidgetsButton = new GuiButtonSpecial(xStart + xRight + 22, yStart + yBottom - 16, 10, 10, "\u25e4",
                b -> toggleShowWidgets());
        allWidgetsButton.setTooltipText(I18n.format("gui.programmer.button.openPanel.tooltip"));
        addButton(allWidgetsButton);

        difficultyButtons = new ArrayList<>();
        for (WidgetDifficulty difficulty : WidgetDifficulty.values()) {
            DifficultyButton dButton = new DifficultyButton(xStart + xRight - 36, yStart + yBottom + 29 + difficulty.ordinal() * 12,
                    0xFF404040, difficulty, b -> updateDifficulty(difficulty));
            dButton.checked = difficulty == PNCConfig.Client.programmerDifficulty;
            addButton(dButton);
            difficultyButtons.add(dButton);
            dButton.otherChoices = difficultyButtons;
            dButton.setTooltip("gui.programmer.difficulty." + difficulty.toString().toLowerCase() + ".tooltip");
        }

        addButton(new Button(xStart + 5, yStart + yBottom + 4, 87, 20, I18n.format("gui.programmer.button.showStart"), b -> gotoStart()));
        addButton(new Button(xStart + 5, yStart + yBottom + 26, 87, 20, I18n.format("gui.programmer.button.showLatest"), b -> gotoLatest()));
        addButton(showInfo = new GuiCheckBox(xStart + 5, yStart + yBottom + 49, 0xFF404040, "gui.programmer.checkbox.showInfo").setChecked(te.showInfo));
        addButton(showFlow = new GuiCheckBox(xStart + 5, yStart + yBottom + 61, 0xFF404040, "gui.programmer.checkbox.showFlow").setChecked(te.showFlow));

        GuiButtonSpecial pastebinButton = new GuiButtonSpecial(guiLeft - 24, guiTop + 44, 20, 20, "",
                b -> pastebin());
        pastebinButton.setTooltipText(I18n.format("gui.remote.button.pastebinButton"));
        pastebinButton.setRenderedIcon(Textures.GUI_PASTEBIN_ICON_LOCATION);
        addButton(pastebinButton);

        undoButton = new GuiButtonSpecial(guiLeft - 24, guiTop + 2, 20, 20, "").withTag("undo");
        redoButton = new GuiButtonSpecial(guiLeft - 24, guiTop + 23, 20, 20, "").withTag("redo");
        GuiButtonSpecial clearAllButton = new GuiButtonSpecial(guiLeft - 24, guiTop + 65, 20, 20, "", b -> clear());
        convertToRelativeButton = new GuiButtonSpecial(guiLeft - 24, guiTop + 86, 20, 20, "Rel", b -> convertToRelative());

        undoButton.setRenderedIcon(Textures.GUI_UNDO_ICON_LOCATION);
        redoButton.setRenderedIcon(Textures.GUI_REDO_ICON_LOCATION);
        clearAllButton.setRenderedIcon(Textures.GUI_DELETE_ICON_LOCATION);

        undoButton.setTooltipText(I18n.format("gui.programmer.button.undoButton.tooltip"));
        redoButton.setTooltipText(I18n.format("gui.programmer.button.redoButton.tooltip"));
        clearAllButton.setTooltipText(I18n.format("gui.programmer.button.clearAllButton.tooltip"));

        addButton(undoButton);
        addButton(redoButton);
        addButton(clearAllButton);
        addButton(convertToRelativeButton);

        addLabel(title.getFormattedText(), guiLeft + 7, guiTop + 5, 0xFF404040);

        nameField = new WidgetTextField(font, guiLeft + xRight - 99, guiTop + 5, 98, font.FONT_HEIGHT);
        addButton(nameField);

        filterField = new FilterTextField(font, guiLeft + 78, guiTop + 26, 100, font.FONT_HEIGHT);
        filterField.setResponder(s -> filterSpawnWidgets());

        addButton(filterField);

        String name = I18n.format("gui.programmer.name");
        addLabel(name, guiLeft + xRight - 102 - font.getStringWidth(name), guiTop + 5, 0xFF404040);

        updateVisibleProgWidgets();

        for (IProgWidget widget : te.progWidgets) {
            if (!programmerUnit.isOutsideProgrammingArea(widget)) {
                return;
            }
        }
        programmerUnit.gotoPiece(findWidget(te.progWidgets, ProgWidgetStart.class));
    }

    private void adjustPage(int dir) {
        widgetPage += dir;
        if (widgetPage < 0) widgetPage = maxPage -1;
        else if (widgetPage >= maxPage) widgetPage = 0;
        updateVisibleProgWidgets();
    }

    private void toggleShowWidgets() {
        showingAllWidgets = !showingAllWidgets;
        allWidgetsButton.setMessage(showingAllWidgets ? "\u25e2" : "\u25e4");
        updateVisibleProgWidgets();
        filterField.setFocused2(showingAllWidgets);
    }

    private void updateDifficulty(WidgetDifficulty difficulty) {
        ConfigHelper.setProgrammerDifficulty(difficulty);
        if (showingAllWidgets) toggleShowWidgets();
        updateVisibleProgWidgets();
    }

    private void gotoLatest() {
        if (te.progWidgets.size() > 0) {
            programmerUnit.gotoPiece(te.progWidgets.get(te.progWidgets.size() - 1));
        }
    }

    private void gotoStart() {
        programmerUnit.gotoPiece(findWidget(te.progWidgets, ProgWidgetStart.class));
    }

    private void pastebin() {
        CompoundNBT mainTag = te.writeProgWidgetsToNBT(new CompoundNBT());
        minecraft.displayGuiScreen(pastebinGui = new GuiPastebin(this, mainTag));
    }

    private void clear() {
        te.progWidgets.clear();
        NetworkHandler.sendToServer(new PacketProgrammerUpdate(te));
    }

    private void convertToRelative() {
        for (IProgWidget widget : te.progWidgets) {
            if (widget instanceof ProgWidgetStart) {
                generateRelativeOperators((ProgWidgetCoordinateOperator) widget.getOutputWidget(), null, false);
                break;
            }
        }
    }

    @Override
    protected Point getInvNameOffset() {
        return null;
    }

    @Override
    protected Point getInvTextOffset() {
        return null;
    }

    @Override
    protected boolean shouldAddProblemTab() {
        return false;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        int xRight = getProgrammerBounds().x + getProgrammerBounds().width; // 299 or 649
        int yBottom = getProgrammerBounds().y + getProgrammerBounds().height; // 171 or 427

        String str = widgetPage + 1 + "/" + maxPage;
        font.drawString(str, xRight + (22 - font.getStringWidth(str) / 2f), yBottom + 4, 0xFF404040);
        font.drawString(I18n.format("gui.programmer.difficulty"), xRight - 36, yBottom + 20, 0xFF404040);

        if (showingWidgetProgress == 0) {
            programmerUnit.renderForeground(x, y, draggingWidget);
        }

        for (int i = 0; i < visibleSpawnWidgets.size(); i++) {
            IProgWidget widget = visibleSpawnWidgets.get(i);
            if (widget != draggingWidget && x - guiLeft >= widget.getX()
                    && y - guiTop >= widget.getY() && x - guiLeft <= widget.getX() + widget.getWidth() / 2
                    && y - guiTop <= widget.getY() + widget.getHeight() / 2
                    && (!showingAllWidgets || filteredSpawnWidgets == null || filteredSpawnWidgets.get(i))) {
                List<ITextComponent> tooltip = new ArrayList<>();
                widget.getTooltip(tooltip);
                ThirdPartyManager.instance().docsProvider.addTooltip(tooltip, showingAllWidgets);
                if (!tooltip.isEmpty()) {
                    drawHoveringString(tooltip.stream().map(ITextComponent::getFormattedText).collect(Collectors.toList()), x - guiLeft, y - guiTop, font);
                }
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (nameField.isFocused() || filterField.isFocused() && keyCode != GLFW.GLFW_KEY_TAB) {
            return true;
        }

        switch (keyCode) {
            case GLFW.GLFW_KEY_I:
                showWidgetDocs();
                return true;
            case GLFW.GLFW_KEY_R:
                if (exportButton.isHovered()) {
                    NetworkHandler.sendToServer(new PacketGuiButton("redstone"));
                }
                return true;
            case GLFW.GLFW_KEY_SPACE:
            case GLFW.GLFW_KEY_TAB:
                toggleShowWidgets();
                return true;
            case GLFW.GLFW_KEY_DELETE:
                IProgWidget widget = programmerUnit.getHoveredWidget(lastMouseX, lastMouseY);
                if (widget != null) {
                    te.progWidgets.remove(widget);
                    NetworkHandler.sendToServer(new PacketProgrammerUpdate(te));
                }
                return true;
            case GLFW.GLFW_KEY_Z:
                NetworkHandler.sendToServer(new PacketGuiButton("undo"));
                return true;
            case GLFW.GLFW_KEY_Y:
                NetworkHandler.sendToServer(new PacketGuiButton("redo"));
                return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void showWidgetDocs() {
        int x = lastMouseX;
        int y = lastMouseY;

        IProgWidget hoveredWidget = programmerUnit.getHoveredWidget(x, y);
        ThirdPartyManager.instance().docsProvider.showWidgetDocs(getWidgetId(hoveredWidget));
        for (IProgWidget widget : visibleSpawnWidgets) {
            if (widget != draggingWidget && x - guiLeft >= widget.getX() && y - guiTop >= widget.getY() && x - guiLeft <= widget.getX() + widget.getWidth() / 2 && y - guiTop <= widget.getY() + widget.getHeight() / 2) {
                ThirdPartyManager.instance().docsProvider.showWidgetDocs(getWidgetId(widget));
                break;
            }
        }
    }

    private String getWidgetId(IProgWidget w) {
        if (w == null) return null;
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, w.getWidgetString());
    }

    @Override
    protected boolean shouldDrawBackground() {
        return false;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        renderBackground();
        bindGuiTexture();
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        blit(xStart, yStart, 0, 0, xSize, ySize, xSize, ySize);

        programmerUnit.getScrollBar().setEnabled(showingWidgetProgress == 0);
        super.drawGuiContainerBackgroundLayer(partialTicks, x, y);
        if (showingWidgetProgress > 0) programmerUnit.getScrollBar().setCurrentState(programmerUnit.getLastZoom());

        programmerUnit.render(x, y, showFlow.checked, showInfo.checked && showingWidgetProgress == 0, draggingWidget == null);

        int origX = x;
        int origY = y;
        x -= programmerUnit.getTranslatedX();
        y -= programmerUnit.getTranslatedY();
        float scale = programmerUnit.getScale();
        x = (int) (x / scale);
        y = (int) (y / scale);

        if (showingWidgetProgress > 0) {
            int xRight = getProgrammerBounds().x + getProgrammerBounds().width; // 299 or 649
            int yBottom = getProgrammerBounds().y + getProgrammerBounds().height; // 171 or 427

            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            bindGuiTexture();
            int width = oldShowingWidgetProgress + (int) ((showingWidgetProgress - oldShowingWidgetProgress) * partialTicks);
            for (int i = 0; i < width; i++) {
                blit(xStart + xRight + 21 - i, yStart + 36, xRight + 24, 36, 1, yBottom - 35, xSize, ySize);
            }
            blit(xStart + xRight + 20 - width, yStart + 36, xRight + 20, 36, 2, yBottom - 35, xSize, ySize);

            if (showingAllWidgets && draggingWidget != null) toggleShowWidgets();
        }
        GlStateManager.enableTexture();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        for (int i = 0; i < visibleSpawnWidgets.size(); i++) {
            IProgWidget widget = visibleSpawnWidgets.get(i);
            GlStateManager.pushMatrix();
            GlStateManager.translated(widget.getX() + guiLeft, widget.getY() + guiTop, 0);
            GlStateManager.scaled(0.5, 0.5, 1);
            if (showingAllWidgets && filteredSpawnWidgets != null && !filteredSpawnWidgets.get(i)) {
                GlStateManager.color4f(1, 1, 1, 0.2f);
            } else {
                GlStateManager.color4f(1, 1, 1, 1);
            }
            widget.render();
            GlStateManager.popMatrix();
        }
        GlStateManager.disableBlend();

        GlStateManager.pushMatrix();
        GlStateManager.translated(programmerUnit.getTranslatedX(), programmerUnit.getTranslatedY(), 0);
        GlStateManager.scaled(scale, scale, 1);
        if (draggingWidget != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translated(draggingWidget.getX() + guiLeft, draggingWidget.getY() + guiTop, 0);
            GlStateManager.scaled(0.5, 0.5, 1);
            draggingWidget.render();
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();

        boolean isLeftClicking = minecraft.gameSettings.keyBindAttack.isKeyDown();
        boolean isMiddleClicking = minecraft.gameSettings.keyBindPickBlock.isKeyDown();

        if (draggingWidget != null) {
            setConnectingWidgetsToXY(draggingWidget, x - dragMouseStartX + dragWidgetStartX - guiLeft, y - dragMouseStartY + dragWidgetStartY - guiTop);
        }

        if (isLeftClicking && !wasClicking) {
            for (IProgWidget widget : visibleSpawnWidgets) {
                if (origX >= widget.getX() + guiLeft && origY >= widget.getY() + guiTop && origX <= widget.getX() + guiLeft + widget.getWidth() / 2 && origY <= widget.getY() + guiTop + widget.getHeight() / 2) {
                    draggingWidget = widget.copy();
                    te.progWidgets.add(draggingWidget);
                    dragMouseStartX = x - (int) (guiLeft / scale);
                    dragMouseStartY = y - (int) (guiTop / scale);
                    dragWidgetStartX = (int) ((widget.getX() - programmerUnit.getTranslatedX()) / scale);
                    dragWidgetStartY = (int) ((widget.getY() - programmerUnit.getTranslatedY()) / scale);
                    break;
                }
            }

            // create area widgets straight from GPS Area Tools
            ItemStack heldItem = minecraft.player.inventory.getItemStack();
            ProgWidgetArea areaToolWidget = heldItem.getItem() instanceof ItemGPSAreaTool ? ItemGPSAreaTool.getArea(heldItem) : null;

            if (draggingWidget == null && showingWidgetProgress == 0) {
                IProgWidget widget = programmerUnit.getHoveredWidget(origX, origY);
                if (widget != null) {
                    draggingWidget = widget;
                    dragMouseStartX = x - guiLeft;
                    dragMouseStartY = y - guiTop;
                    dragWidgetStartX = widget.getX();
                    dragWidgetStartY = widget.getY();

                    if (areaToolWidget != null && widget instanceof ProgWidgetArea) {
                        CompoundNBT tag = new CompoundNBT();
                        areaToolWidget.writeToNBT(tag);
                        widget.readFromNBT(tag);
                    } else if (heldItem.getItem() == ModItems.GPS_TOOL) {
                        if (widget instanceof ProgWidgetCoordinate) {
                            ((ProgWidgetCoordinate) widget).loadFromGPSTool(heldItem);
                        } else if (widget instanceof ProgWidgetArea) {
                            BlockPos pos = ItemGPSTool.getGPSLocation(heldItem);
                            String var = ItemGPSTool.getVariable(heldItem);
                            if (pos != null) ((ProgWidgetArea) widget).setP1(pos);
                            ((ProgWidgetArea) widget).setP2(BlockPos.ZERO);
                            ((ProgWidgetArea) widget).setCoord1Variable(var);
                            ((ProgWidgetArea) widget).setCoord2Variable("");
                        }
                    }
                }
            }

            // Create a new widget from a GPS Area tool when nothing was selected
            if (draggingWidget == null) {
                if (areaToolWidget != null) {
                    draggingWidget = areaToolWidget;
                } else if (heldItem.getItem() == ModItems.GPS_TOOL) {
                    if (PneumaticCraftRepressurized.proxy.isSneakingInGui()) {
                        BlockPos pos = ItemGPSTool.getGPSLocation(heldItem);
                        ProgWidgetArea areaWidget = ProgWidgetArea.fromPositions(pos, BlockPos.ZERO);
                        String var = ItemGPSTool.getVariable(heldItem);
                        if (!var.isEmpty()) areaWidget.setCoord1Variable(var);
                        draggingWidget = areaWidget;
                    } else {
                        ProgWidgetCoordinate coordWidget = new ProgWidgetCoordinate();
                        draggingWidget = coordWidget;
                        coordWidget.loadFromGPSTool(heldItem);
                    }
                }

                if (draggingWidget != null) {
                    draggingWidget.setX(Integer.MAX_VALUE);
                    draggingWidget.setY(Integer.MAX_VALUE);
                    te.progWidgets.add(draggingWidget);
                    dragMouseStartX = draggingWidget.getWidth() / 3;
                    dragMouseStartY = draggingWidget.getHeight() / 4;
                    dragWidgetStartX = 0;
                    dragWidgetStartY = 0;
                }
            }
        } else if (isMiddleClicking && !wasClicking && showingWidgetProgress == 0) {
            IProgWidget widget = programmerUnit.getHoveredWidget(origX, origY);
            if (widget != null) {
                draggingWidget = widget.copy();
                te.progWidgets.add(draggingWidget);
                dragMouseStartX = 0;
                dragMouseStartY = 0;
                dragWidgetStartX = widget.getX() - (x - guiLeft);
                dragWidgetStartY = widget.getY() - (y - guiTop);
                if (PneumaticCraftRepressurized.proxy.isSneakingInGui()) copyAndConnectConnectingWidgets(widget, draggingWidget);
            }
        } else if (isMiddleClicking && showingAllWidgets) {
            showWidgetDocs();
        }

        if (!isLeftClicking && !isMiddleClicking && draggingWidget != null) {
            if (programmerUnit.isOutsideProgrammingArea(draggingWidget)) {
                deleteConnectingWidgets(draggingWidget);
            } else {
                handlePuzzleMargins();
                if (!isValidPlaced(draggingWidget)) {
                    setConnectingWidgetsToXY(draggingWidget, dragWidgetStartX, dragWidgetStartY);
                    if (programmerUnit.isOutsideProgrammingArea(draggingWidget))
                        deleteConnectingWidgets(draggingWidget);
                }
            }
            NetworkHandler.sendToServer(new PacketProgrammerUpdate(te));
            TileEntityProgrammer.updatePuzzleConnections(te.progWidgets);

            draggingWidget = null;
        }
        wasClicking = isLeftClicking || isMiddleClicking;
        lastMouseX = origX;
        lastMouseY = origY;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isValidPlaced(IProgWidget widget1) {
        Rectangle draggingRect = new Rectangle(widget1.getX(), widget1.getY(), widget1.getWidth() / 2, widget1.getHeight() / 2);
        for (IProgWidget widget : te.progWidgets) {
            if (widget != widget1) {
                if (draggingRect.intersects(widget.getX(), widget.getY(), widget.getWidth() / 2.0, widget.getHeight() / 2.0)) {
                    return false;
                }
            }
        }
        IProgWidget[] parameters = widget1.getConnectedParameters();
        if (parameters != null) {
            for (IProgWidget widget : parameters) {
                if (widget != null && !isValidPlaced(widget)) return false;
            }
        }
        IProgWidget outputWidget = widget1.getOutputWidget();
        return !(outputWidget != null && !isValidPlaced(outputWidget));
    }

    private void handlePuzzleMargins() {
        //Check for connection to the left of the dragged widget.
        Class<? extends IProgWidget> returnValue = draggingWidget.returnType();
        if (returnValue != null) {
            for (IProgWidget widget : te.progWidgets) {
                if (widget != draggingWidget && Math.abs(widget.getX() + widget.getWidth() / 2 - draggingWidget.getX()) <= FAULT_MARGIN) {
                    Class<? extends IProgWidget>[] parameters = widget.getParameters();
                    if (parameters != null) {
                        for (int i = 0; i < parameters.length; i++) {
                            if (widget.canSetParameter(i) && parameters[i] == returnValue && Math.abs(widget.getY() + i * 11 - draggingWidget.getY()) <= FAULT_MARGIN) {
                                setConnectingWidgetsToXY(draggingWidget, widget.getX() + widget.getWidth() / 2, widget.getY() + i * 11);
                                return;
                            }
                        }
                    }
                }
            }
        }

        //check for connection to the right of the dragged widget.
        Class<? extends IProgWidget>[] parameters = draggingWidget.getParameters();
        if (parameters != null) {
            for (IProgWidget widget : te.progWidgets) {
                IProgWidget outerPiece = draggingWidget;
                if (outerPiece.returnType() != null) {//When the piece is a parameter pice (area, item filter, text).
                    while (outerPiece.getConnectedParameters()[0] != null) {
                        outerPiece = outerPiece.getConnectedParameters()[0];
                    }
                }
                if (widget != draggingWidget && Math.abs(outerPiece.getX() + outerPiece.getWidth() / 2 - widget.getX()) <= FAULT_MARGIN) {
                    if (widget.returnType() != null) {
                        for (int i = 0; i < parameters.length; i++) {
                            if (draggingWidget.canSetParameter(i) && parameters[i] == widget.returnType() && Math.abs(draggingWidget.getY() + i * 11 - widget.getY()) <= FAULT_MARGIN) {
                                setConnectingWidgetsToXY(draggingWidget, widget.getX() - draggingWidget.getWidth() / 2 - (outerPiece.getX() - draggingWidget.getX()), widget.getY() - i * 11);
                            }
                        }
                    } else {
                        Class<? extends IProgWidget>[] checkingPieceParms = widget.getParameters();
                        if (checkingPieceParms != null) {
                            for (int i = 0; i < checkingPieceParms.length; i++) {
                                if (widget.canSetParameter(i + parameters.length) && checkingPieceParms[i] == parameters[0] && Math.abs(widget.getY() + i * 11 - draggingWidget.getY()) <= FAULT_MARGIN) {
                                    setConnectingWidgetsToXY(draggingWidget, widget.getX() - draggingWidget.getWidth() / 2 - (outerPiece.getX() - draggingWidget.getX()), widget.getY() + i * 11);
                                }
                            }
                        }
                    }
                }
            }
        }

        //check for connection to the top of the dragged widget.
        if (draggingWidget.hasStepInput()) {
            for (IProgWidget widget : te.progWidgets) {
                if (widget.hasStepOutput() && Math.abs(widget.getX() - draggingWidget.getX()) <= FAULT_MARGIN && Math.abs(widget.getY() + widget.getHeight() / 2 - draggingWidget.getY()) <= FAULT_MARGIN) {
                    setConnectingWidgetsToXY(draggingWidget, widget.getX(), widget.getY() + widget.getHeight() / 2);
                }
            }
        }

        //check for connection to the bottom of the dragged widget.
        if (draggingWidget.hasStepOutput()) {
            for (IProgWidget widget : te.progWidgets) {
                if (widget.hasStepInput() && Math.abs(widget.getX() - draggingWidget.getX()) <= FAULT_MARGIN && Math.abs(widget.getY() - draggingWidget.getY() - draggingWidget.getHeight() / 2) <= FAULT_MARGIN) {
                    setConnectingWidgetsToXY(draggingWidget, widget.getX(), widget.getY() - draggingWidget.getHeight() / 2);
                }
            }
        }
    }

    private void setConnectingWidgetsToXY(IProgWidget widget, int x, int y) {
        widget.setX(x);
        widget.setY(y);
        IProgWidget[] connectingWidgets = widget.getConnectedParameters();
        if (connectingWidgets != null) {
            for (int i = 0; i < connectingWidgets.length; i++) {
                if (connectingWidgets[i] != null) {
                    if (i < connectingWidgets.length / 2) {
                        setConnectingWidgetsToXY(connectingWidgets[i], x + widget.getWidth() / 2, y + i * 11);
                    } else {
                        int totalWidth = 0;
                        IProgWidget branch = connectingWidgets[i];
                        while (branch != null) {
                            totalWidth += branch.getWidth() / 2;
                            branch = branch.getConnectedParameters()[0];
                        }
                        setConnectingWidgetsToXY(connectingWidgets[i], x - totalWidth, y + (i - connectingWidgets.length / 2) * 11);
                    }
                }
            }
        }
        IProgWidget outputWidget = widget.getOutputWidget();
        if (outputWidget != null) setConnectingWidgetsToXY(outputWidget, x, y + widget.getHeight() / 2);
    }

    private void copyAndConnectConnectingWidgets(IProgWidget original, IProgWidget copy) {
        IProgWidget[] connectingWidgets = original.getConnectedParameters();
        if (connectingWidgets != null) {
            for (int i = 0; i < connectingWidgets.length; i++) {
                if (connectingWidgets[i] != null) {
                    IProgWidget c = connectingWidgets[i].copy();
                    te.progWidgets.add(c);
                    copy.setParameter(i, c);
                    copyAndConnectConnectingWidgets(connectingWidgets[i], c);
                }
            }
        }
        IProgWidget outputWidget = original.getOutputWidget();
        if (outputWidget != null) {
            IProgWidget c = outputWidget.copy();
            te.progWidgets.add(c);
            copy.setOutputWidget(c);
            copyAndConnectConnectingWidgets(outputWidget, c);
        }
    }

    private void deleteConnectingWidgets(IProgWidget widget) {
        te.progWidgets.remove(widget);
        IProgWidget[] connectingWidgets = widget.getConnectedParameters();
        if (connectingWidgets != null) {
            for (IProgWidget widg : connectingWidgets) {
                if (widg != null) deleteConnectingWidgets(widg);
            }
        }
        IProgWidget outputWidget = widget.getOutputWidget();
        if (outputWidget != null) deleteConnectingWidgets(outputWidget);
    }

    @Override
    public void tick() {
        super.tick();

        if (te.recentreStartPiece) {
            programmerUnit.gotoPiece(findWidget(te.progWidgets, ProgWidgetStart.class));
            te.recentreStartPiece = false;
        }

        undoButton.active = te.canUndo;
        redoButton.active = te.canRedo;

        updateConvertRelativeState();

        ItemStack programmedItem = te.getIteminProgrammingSlot();
        oldShowingWidgetProgress = showingWidgetProgress;
        if (showingAllWidgets) {
            int maxProgress = maxPage * WIDGET_X_SPACING;
            if (showingWidgetProgress < maxProgress) {
                showingWidgetProgress += 60;
                if (showingWidgetProgress >= maxProgress) {
                    showingWidgetProgress = maxProgress;
                    updateVisibleProgWidgets();
                }
            }
        } else {
            showingWidgetProgress -= 60;
            if (showingWidgetProgress < 0) showingWidgetProgress = 0;
        }

        List<ITextComponent> errors = new ArrayList<>();
        List<ITextComponent> warnings = new ArrayList<>();
        for (IProgWidget w : te.progWidgets) {
            w.addErrors(errors, te.progWidgets);
            w.addWarnings(warnings, te.progWidgets);
        }

        boolean isDeviceInserted = !programmedItem.isEmpty();
        importButton.active = isDeviceInserted;
        exportButton.active = isDeviceInserted && errors.size() == 0;

        updateExportButtonTooltip(programmedItem, errors, warnings);

        if (!programmedItem.isEmpty()) {
            nameField.setEnabled(true);
            if (!nameField.isFocused()) {
                if (wasFocused) {
                    programmedItem.setDisplayName(new StringTextComponent(nameField.getText()));
                    NetworkHandler.sendToServer(new PacketUpdateTextfield(te, 0));
                }
                nameField.setText(programmedItem.getDisplayName().getFormattedText());
                wasFocused = false;
            } else {
                wasFocused = true;
            }
        } else {
            nameField.setEnabled(false);
            nameField.setText("");
            wasFocused = false;
        }
    }

    private void updateExportButtonTooltip(ItemStack programmedItem, List<ITextComponent> errors, List<ITextComponent> warnings) {
        List<String> exportButtonTooltip = new ArrayList<>();
        exportButtonTooltip.add("Export program");
        exportButtonTooltip.add(I18n.format("gui.programmer.button.export.programmingWhen", I18n.format("gui.programmer.button.export." + (te.redstoneMode == 0 ? "pressingButton" : "onItemInsert"))));
        exportButtonTooltip.add(I18n.format("gui.programmer.button.export.pressRToChange"));
        if (!programmedItem.isEmpty()) {
            int required = te.getRequiredPuzzleCount();
            if (required != 0) exportButtonTooltip.add("");
            int r = minecraft.player.isCreative() ? 0 : required;
            if (required > 0) {
                exportButtonTooltip.add(I18n.format("gui.tooltip.programmable.requiredPieces", r));
            } else if (required < 0) {
                exportButtonTooltip.add(I18n.format("gui.tooltip.programmable.returnedPieces", r));
            }
            if (required != 0 && minecraft.player.isCreative()) exportButtonTooltip.add("(Creative mode)");
        } else {
            exportButtonTooltip.add(TextFormatting.GOLD + "No programmable item inserted.");
        }

        if (errors.size() > 0)
            exportButtonTooltip.add(TextFormatting.RED + I18n.format("gui.programmer.errorCount", errors.size()));
        if (warnings.size() > 0)
            exportButtonTooltip.add(TextFormatting.YELLOW + I18n.format("gui.programmer.warningCount", warnings.size()));

        exportButton.setTooltipText(exportButtonTooltip);
    }

    private void updateConvertRelativeState() {
        convertToRelativeButton.active = false;
        List<String> tooltip = new ArrayList<>();
        tooltip.add("gui.programmer.button.convertToRelative.desc");

        boolean startFound = false;
        for (IProgWidget startWidget : te.progWidgets) {
            if (startWidget instanceof ProgWidgetStart) {
                startFound = true;
                IProgWidget widget = startWidget.getOutputWidget();
                if (widget instanceof ProgWidgetCoordinateOperator) {
                    ProgWidgetCoordinateOperator operatorWidget = (ProgWidgetCoordinateOperator) widget;
                    if (!operatorWidget.getVariable().equals("")) {
                        try {
                            if (generateRelativeOperators(operatorWidget, tooltip, true)) {
                                convertToRelativeButton.active = true;
                            } else {
                                tooltip.add("gui.programmer.button.convertToRelative.notEnoughRoom");
                            }
                        } catch (NullPointerException e) {
                            tooltip.add("gui.programmer.button.convertToRelative.cantHaveVariables");
                        }
                    } else {
                        tooltip.add("gui.programmer.button.convertToRelative.noVariableName");
                    }
                } else {
                    tooltip.add("gui.programmer.button.convertToRelative.noBaseCoordinate");
                }
            }
        }
        if (!startFound) tooltip.add("gui.programmer.button.convertToRelative.noStartPiece");

        List<String> localizedTooltip = new ArrayList<>();
        for (String s : tooltip) {
            localizedTooltip.addAll(PneumaticCraftUtils.convertStringIntoList(I18n.format(s), 40));
        }
        convertToRelativeButton.setTooltipText(localizedTooltip);
    }

    private boolean generateRelativeOperators(ProgWidgetCoordinateOperator baseWidget, List<String> tooltip, boolean simulate) {
        BlockPos baseCoord = ProgWidgetCoordinateOperator.calculateCoordinate(baseWidget, 0, baseWidget.getOperator());
        Map<BlockPos, String> offsetToVariableNames = new HashMap<>();
        for (IProgWidget widget : te.progWidgets) {
            if (widget instanceof ProgWidgetArea) {
                ProgWidgetArea area = (ProgWidgetArea) widget;
                if (area.getCoord1Variable().equals("") && (area.x1 != 0 || area.y1 != 0 || area.z1 != 0)) {
                    BlockPos offset = new BlockPos(area.x1 - baseCoord.getX(), area.y1 - baseCoord.getY(), area.z1 - baseCoord.getZ());
                    String var = getOffsetVariable(offsetToVariableNames, baseWidget.getVariable(), offset);
                    if (!simulate) area.setCoord1Variable(var);
                }
                if (area.getCoord2Variable().equals("") && (area.x2 != 0 || area.y2 != 0 || area.z2 != 0)) {
                    BlockPos offset = new BlockPos(area.x2 - baseCoord.getX(), area.y2 - baseCoord.getY(), area.z2 - baseCoord.getZ());
                    String var = getOffsetVariable(offsetToVariableNames, baseWidget.getVariable(), offset);
                    if (!simulate) area.setCoord2Variable(var);
                }
            } else if (widget instanceof ProgWidgetCoordinate && baseWidget.getConnectedParameters()[0] != widget) {
                ProgWidgetCoordinate coordinate = (ProgWidgetCoordinate) widget;
                if (!coordinate.isUsingVariable()) {
                    BlockPos c = coordinate.getCoordinate();
                    String chunkString = "(" + c.getX() + ", " + c.getY() + ", " + c.getZ() + ")";
                    if (PneumaticCraftUtils.distBetween(c, 0, 0, 0) < 64) {
                        // When the coordinate value is close to 0, there's a low chance it means a position, and rather an offset.
                        if (tooltip != null)
                            tooltip.add(I18n.format("gui.programmer.button.convertToRelative.coordIsNotChangedWarning", chunkString));
                    } else {
                        if (tooltip != null)
                            tooltip.add(I18n.format("gui.programmer.button.convertToRelative.coordIsChangedWarning", chunkString));
                        if (!simulate) {
                            BlockPos offset = new BlockPos(c.getX() - baseCoord.getX(), c.getY() - baseCoord.getY(), c.getZ() - baseCoord.getZ());
                            String var = getOffsetVariable(offsetToVariableNames, baseWidget.getVariable(), offset);
                            coordinate.setVariable(var);
                            coordinate.setUsingVariable(true);
                        }
                    }
                }
            }
        }
        if (offsetToVariableNames.size() > 0) {
            ProgWidgetCoordinateOperator firstOperator = null;
            ProgWidgetCoordinateOperator prevOperator = baseWidget;
            int x = baseWidget.getX();
            for (Map.Entry<BlockPos, String> entry : offsetToVariableNames.entrySet()) {
                ProgWidgetCoordinateOperator operator = new ProgWidgetCoordinateOperator();
                operator.setVariable(entry.getValue());

                int y = prevOperator.getY() + prevOperator.getHeight() / 2;
                operator.setX(x);
                operator.setY(y);
                if (!isValidPlaced(operator)) return false;

                ProgWidgetCoordinate coordinatePiece1 = new ProgWidgetCoordinate();
                coordinatePiece1.setX(x + prevOperator.getWidth() / 2);
                coordinatePiece1.setY(y);
                coordinatePiece1.setVariable(baseWidget.getVariable());
                coordinatePiece1.setUsingVariable(true);
                if (!isValidPlaced(coordinatePiece1)) return false;

                ProgWidgetCoordinate coordinatePiece2 = new ProgWidgetCoordinate();
                coordinatePiece2.setX(x + prevOperator.getWidth() / 2 + coordinatePiece1.getWidth() / 2);
                coordinatePiece2.setY(y);
                coordinatePiece2.setCoordinate(entry.getKey());
                if (!isValidPlaced(coordinatePiece2)) return false;

                if (!simulate) {
                    te.progWidgets.add(operator);
                    te.progWidgets.add(coordinatePiece1);
                    te.progWidgets.add(coordinatePiece2);
                }
                if (firstOperator == null) firstOperator = operator;
                prevOperator = operator;
            }
            if (!simulate) {
                NetworkHandler.sendToServer(new PacketProgrammerUpdate(te));
                TileEntityProgrammer.updatePuzzleConnections(te.progWidgets);
            }
            return true;
        } else {
            return true; //When there's nothing to place there's always room.
        }
    }

    private String getOffsetVariable(Map<BlockPos, String> offsetToVariableNames, String baseVariable, BlockPos offset) {
        if (offset.equals(BlockPos.ZERO))
            return baseVariable;
        return offsetToVariableNames.computeIfAbsent(offset, k -> "var" + (offsetToVariableNames.size() + 1));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1 && showingWidgetProgress == 0) {
            IProgWidget widget = programmerUnit.getHoveredWidget((int)mouseX, (int)mouseY);
            if (widget != null) {
                GuiProgWidgetOptionBase gui = ProgWidgetGuiManager.getGui(widget, this);
                if (gui != null) minecraft.displayGuiScreen(gui);
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        te.translatedX = programmerUnit.getTranslatedX();
        te.translatedY = programmerUnit.getTranslatedY();
        te.zoomState = programmerUnit.getLastZoom();
        te.showFlow = showFlow.checked;
        te.showInfo = showInfo.checked;
        super.onClose();
    }

    public static IProgWidget findWidget(List<IProgWidget> widgets, Class<? extends IProgWidget> cls) {
        for (IProgWidget w : widgets) {
            if (cls.isAssignableFrom(w.getClass())) return w;
        }
        return null;
    }

    private class FilterTextField extends WidgetTextField {
        FilterTextField(FontRenderer font, int x, int y, int width, int height) {
            super(font, x, y, width, height);
        }

        @Override
        public void renderButton(int x, int y, float partialTicks) {
            // this is needed to force the textfield to draw on top of any
            // widgets in the programming area
            GlStateManager.translated(0, 0, 300);
            super.renderButton(x, y, partialTicks);
            GlStateManager.translated(0, 0, -300);
        }
    }

    private class DifficultyButton extends GuiRadioButton {
        final WidgetDifficulty difficulty;

        public DifficultyButton(int x, int y, int color, WidgetDifficulty difficulty, Consumer<GuiRadioButton> pressable) {
            super(x, y, color, difficulty.getTranslationKey(), pressable);
            this.difficulty = difficulty;
        }
    }

}
