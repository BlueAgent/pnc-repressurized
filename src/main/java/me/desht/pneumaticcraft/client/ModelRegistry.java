package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.block.BlockPneumaticCraftCamo;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.fluid.Fluids;
import me.desht.pneumaticcraft.common.item.ItemPneumaticSubtyped;
import me.desht.pneumaticcraft.common.item.Itemss;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.Map;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

@Mod.EventBusSubscriber
@SideOnly(Side.CLIENT)
public class ModelRegistry {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        registerFluids();

        for (Block block : Blockss.blocks) {
            Item item = Item.getItemFromBlock(block);
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }

        for (Item item: Itemss.items) {
            if (item instanceof ItemPneumaticSubtyped) {
                ModelBakery.registerItemVariants(item);
                ItemPneumaticSubtyped subtyped = (ItemPneumaticSubtyped) item;
                NonNullList<ItemStack> stacks = NonNullList.create();
                item.getSubItems(PneumaticCraftRepressurized.tabPneumaticCraft, stacks);
                for (ItemStack stack : stacks) {
                    ModelLoader.setCustomModelResourceLocation(item, stack.getMetadata(),
                            new ModelResourceLocation(RL(subtyped.getSubtypeModelName(stack.getMetadata())), "inventory"));
                }
            } else {
                ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
            }
        }

        ModelLoader.setCustomStateMapper(Blockss.DRONE_REDSTONE_EMITTER, blockIn -> Collections.emptyMap());
        ModelLoader.setCustomStateMapper(Blockss.KEROSENE_LAMP_LIGHT, blockIn -> Collections.emptyMap());
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        for (Block block : Blockss.blocks) {
            if (block instanceof BlockPneumaticCraftCamo) {
                Map<IBlockState,ModelResourceLocation> map
                        = event.getModelManager().getBlockModelShapes().getBlockStateMapper().getVariants(block);
                for (Map.Entry<IBlockState,ModelResourceLocation> entry : map.entrySet()) {
                    Object object = event.getModelRegistry().getObject(entry.getValue());
                    if (object != null) {
                        IBakedModel existing = (IBakedModel) object;
                        CamoModel customModel = new CamoModel(existing);
                        event.getModelRegistry().putObject(entry.getValue(), customModel);
                    }
                }
            }
        }
    }

    private static void registerFluids() {
        for (IFluidBlock fluidBlock : Fluids.MOD_FLUID_BLOCKS) {
            final Item item = Item.getItemFromBlock((Block) fluidBlock);
            assert item != null;

            ModelBakery.registerItemVariants(item);

            FluidStateMapper stateMapper = new FluidStateMapper(fluidBlock.getFluid());
            ModelLoader.setCustomMeshDefinition(item, stateMapper);
            ModelLoader.setCustomStateMapper((Block) fluidBlock, stateMapper);
        }
    }
}
