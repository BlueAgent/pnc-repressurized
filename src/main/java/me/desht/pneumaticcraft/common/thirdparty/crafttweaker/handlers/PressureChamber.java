package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.handlers;

import static com.blamejared.mtlib.helpers.InputHelper.toStacks;

import java.util.List;
import java.util.stream.Stream;

import com.blamejared.mtlib.helpers.InputHelper;
import com.blamejared.mtlib.helpers.LogHelper;
import com.blamejared.mtlib.helpers.StackHelper;
import com.blamejared.mtlib.utils.BaseListAddition;
import com.blamejared.mtlib.utils.BaseListRemoval;

import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import me.desht.pneumaticcraft.common.recipes.PneumaticRecipeRegistry;
import me.desht.pneumaticcraft.common.recipes.PressureChamberRecipe;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.CraftTweaker;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.RemoveAllRecipes;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.pneumaticcraft.pressurechamber")
@ModOnly("mtlib")
@ZenRegister
public class PressureChamber {
	
	public static final String name = "PneumaticCraft Pressure Chamber";
	
	
    @ZenMethod
    public static void addRecipe(IItemStack[] input, double pressure, IItemStack[] output, boolean asBlock)
    {
    	CraftTweaker.ADDITIONS.add(new Add(new PressureChamberRecipe(toStacks(input), (float) pressure, toStacks(output), asBlock)));
    }
    
    @ZenMethod
    public static void removeRecipe(IIngredient[] output)
    {
    	CraftTweaker.REMOVALS.add(new Remove(PneumaticRecipeRegistry.getInstance().chamberRecipes, output));
    }
    
    @ZenMethod
    public static void removeAllRecipes() {
        CraftTweaker.REMOVALS.add(new RemoveAllRecipes<PressureChamberRecipe>(PressureChamber.name, PneumaticRecipeRegistry.getInstance().chamberRecipes));
    }   
    
    private static class Add extends BaseListAddition<PressureChamberRecipe> {
        public Add(PressureChamberRecipe recipe) {
            super(PressureChamber.name, PneumaticRecipeRegistry.getInstance().chamberRecipes);
            recipes.add(recipe);
        }

        @Override
        public String getRecipeInfo(PressureChamberRecipe recipe) {
            return LogHelper.getStackDescription(recipe.output[0]);
        }
    }
    
    private static class Remove extends BaseListRemoval<PressureChamberRecipe> {
    	private final IIngredient[] output;
    	
        public Remove(List<PressureChamberRecipe> recipes, IIngredient[] output) {
            super(PressureChamber.name, PneumaticRecipeRegistry.getInstance().chamberRecipes, recipes);
            this.output = output;
        }
        
        @Override
        public void apply() {
        	addRecipes();
        	
        	super.apply();
        }
        
        private void addRecipes() {
            for (PressureChamberRecipe r : list) {
            	
            	if(Stream.of(output).allMatch(o -> Stream.of(r.output).anyMatch(ro -> StackHelper.matches(o, InputHelper.toIItemStack(ro))))) {
            		recipes.add(r);
            	}
            }
            
            if(recipes.isEmpty()) {
            	LogHelper.logWarning(String.format("No %s Recipe found for %s. Command ignored!", name, LogHelper.getStackDescription(output)));
            } else {
            	LogHelper.logInfo(String.format("Found %d %s Recipe(s) for %s.", recipes.size(), name, LogHelper.getStackDescription(output)));
            }
		}
        
        @Override
        public String getRecipeInfo(PressureChamberRecipe recipe) {
            return LogHelper.getStackDescription(recipe.output[0]);
        }
        
		@Override
		public String describe() {
			return String.format("Removing %s Recipe(s) for %s", this.name, LogHelper.getStackDescription(output[0]));
		}
    }
}
