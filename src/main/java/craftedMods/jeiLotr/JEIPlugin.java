/*
 * Copyright (C) 2020-2020 CraftedMods (see http://github.com/CraftedMods)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package craftedMods.jeiLotr;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import lotr.client.gui.inv.*;
import lotr.common.init.*;
import lotr.common.inv.*;
import lotr.common.recipe.*;
import lotr.common.tileentity.*;
import mezz.jei.api.*;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.gui.handlers.*;
import mezz.jei.api.helpers.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Vector4f;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.RegistryObject;

@JeiPlugin
public class JEIPlugin implements IModPlugin
{

    private static Method getRecipesMethod;
    private static Field theForgeField;

    static
    {
        try
        {
            try
            {
                getRecipesMethod = RecipeManager.class.getDeclaredMethod ("getRecipes", IRecipeType.class);
            }
            catch (NoSuchMethodException e)
            {
                // Trying the obfuscated version
                getRecipesMethod = RecipeManager.class.getDeclaredMethod ("func_215366_a", IRecipeType.class);
            }
            getRecipesMethod.setAccessible (true);
        }
        catch (Exception e)
        {
            getRecipesMethod = null;
            e.printStackTrace ();
        }

        try
        {
            theForgeField = AbstractAlloyForgeContainer.class.getDeclaredField ("theForge");
            theForgeField.setAccessible (true);
        }
        catch (Exception e)
        {
            theForgeField = null;
            e.printStackTrace ();
        }
    }

    private Collection<LOTRDevice> devices;
    private Map<Block, LOTRDevice> devicesByBlock;

    @Override
    public ResourceLocation getPluginUid ()
    {
        return new ResourceLocation ("crafted_mods", "jei_lotr");
    }

    @SuppressWarnings("unchecked")
    public void initDevices ()
    {
        if (devices == null)
        {
            devices = new HashSet<> ();
            devicesByBlock = new HashMap<> ();

            try
            {
                for (Field field : LOTRRecipes.class.getDeclaredFields ())
                {
                    if (field.getType () == FactionTableType.class && field.getName ().contains ("CRAFTING"))
                    {
                        FactionTableType type = (FactionTableType) field.get (null);

                        Collection<IRecipeType<?>> types = new ArrayList<> ();
                        types.add (type);
                        types.addAll (type.getMultiTableTypes ());

                        RegistryObject<ContainerType<LOTRContainerCraftingTable>> container = null;

                        try
                        {
                            Field containerField = LOTRContainers.class.getDeclaredField (field.getName ());
                            container = (RegistryObject<ContainerType<LOTRContainerCraftingTable>>) containerField
                                .get (null);
                        }
                        catch (NoSuchFieldException e)
                        {
                            JEILotr.LOGGER.warn ("No container field named \"" + field.getName () + "\" was found");
                        }

                        LOTRCraftingTable device = new LOTRCraftingTable (
                            new ResourceLocation ("lotr", type.recipeID.split (":")[1]),
                            type.getIcon (), types, container);

                        devices.add (device);
                        devicesByBlock.put (Block.getBlockFromItem (type.getIcon ().getItem ()), device);
                    }
                }
                JEILotr.LOGGER.debug ("Found " + devices.size () + " faction crafting tables");
            }
            catch (Exception e)
            {
                JEILotr.LOGGER
                    .error ("Couldn't instantiate the faction crafting tables from the LOTRRecipes type fields", e);
            }

            LOTRAlloyForge dwarvenForgeDevice = new LOTRAlloyForge (new ResourceLocation ("lotr", "dwarven_forge"),
                new ItemStack (LOTRBlocks.DWARVEN_FORGE.get ()),
                Arrays.asList (IRecipeType.SMELTING, LOTRRecipes.DWARVEN_FORGE, LOTRRecipes.ALLOY_FORGE,
                    LOTRRecipes.DWARVEN_FORGE_ALLOY),
                new DwarvenForgeTileEntity ());
            LOTRAlloyForge orcForgeDevice = new LOTRAlloyForge (new ResourceLocation ("lotr", "orc_forge"),
                new ItemStack (LOTRBlocks.ORC_FORGE.get ()),
                Arrays.asList (IRecipeType.SMELTING, LOTRRecipes.ORC_FORGE, LOTRRecipes.ALLOY_FORGE,
                    LOTRRecipes.ORC_FORGE_ALLOY),
                new OrcForgeTileEntity ());
            LOTRAlloyForge elvenForgeDevice = new LOTRAlloyForge (new ResourceLocation ("lotr", "elven_forge"),
                new ItemStack (LOTRBlocks.ELVEN_FORGE.get ()),
                Arrays.asList (IRecipeType.SMELTING, LOTRRecipes.ELVEN_FORGE, LOTRRecipes.ALLOY_FORGE,
                    LOTRRecipes.ELVEN_FORGE_ALLOY),
                new ElvenForgeTileEntity ());
            LOTRAlloyForge alloyForgeDevice = new LOTRAlloyForge (new ResourceLocation ("lotr", "alloy_forge"),
                new ItemStack (LOTRBlocks.ALLOY_FORGE.get ()),
                Arrays.asList (LOTRRecipes.ALLOY_FORGE, IRecipeType.SMELTING), new AlloyForgeTileEntity ());
            LOTRAlloyForge hobbitOvenDevice = new LOTRAlloyForge (new ResourceLocation ("lotr", "hobbit_oven"),
                new ItemStack (LOTRBlocks.HOBBIT_OVEN.get ()),
                Arrays.asList (LOTRRecipes.HOBBIT_OVEN, LOTRRecipes.HOBBIT_OVEN_ALLOY, IRecipeType.SMELTING),
                new HobbitOvenTileEntity ());

            devices.add (dwarvenForgeDevice);
            devices.add (orcForgeDevice);
            devices.add (elvenForgeDevice);
            devices.add (alloyForgeDevice);
            devices.add (hobbitOvenDevice);

            devicesByBlock.put (Block.getBlockFromItem (dwarvenForgeDevice.icon.getItem ()), dwarvenForgeDevice);
            devicesByBlock.put (Block.getBlockFromItem (orcForgeDevice.icon.getItem ()), orcForgeDevice);
            devicesByBlock.put (Block.getBlockFromItem (elvenForgeDevice.icon.getItem ()), elvenForgeDevice);
            devicesByBlock.put (Block.getBlockFromItem (alloyForgeDevice.icon.getItem ()), alloyForgeDevice);
            devicesByBlock.put (Block.getBlockFromItem (hobbitOvenDevice.icon.getItem ()), hobbitOvenDevice);

            LOTRKeg keg = new LOTRKeg (new ResourceLocation ("lotr", "keg"), new ItemStack (LOTRBlocks.KEG.get ()),
                Arrays.asList (LOTRRecipes.DRINK_BREWING));

            devices.add (keg);
        }
    }

    @Override
    public void registerRecipeCatalysts (IRecipeCatalystRegistration registration)
    {
        initDevices ();
        devices.forEach (device -> registration.addRecipeCatalyst (device.icon.copy (), device.uid));

        registration.addRecipeCatalyst (new ItemStack (LOTRBlocks.DWARVEN_FORGE.get ()), VanillaRecipeCategoryUid.FUEL);
        registration.addRecipeCatalyst (new ItemStack (LOTRBlocks.ORC_FORGE.get ()), VanillaRecipeCategoryUid.FUEL);
        registration.addRecipeCatalyst (new ItemStack (LOTRBlocks.ELVEN_FORGE.get ()), VanillaRecipeCategoryUid.FUEL);
        registration.addRecipeCatalyst (new ItemStack (LOTRBlocks.ALLOY_FORGE.get ()), VanillaRecipeCategoryUid.FUEL);
        registration.addRecipeCatalyst (new ItemStack (LOTRBlocks.HOBBIT_OVEN.get ()), VanillaRecipeCategoryUid.FUEL);
    }

    @Override
    public void registerCategories (IRecipeCategoryRegistration registration)
    {
        initDevices ();
        devices.forEach (device ->
        {
            registration
                .addRecipeCategories (device.createCategoryInstance (registration.getJeiHelpers ().getGuiHelper (),
                    registration.getJeiHelpers ().getModIdHelper ()));
        });
    }

    @Override
    public void registerRecipes (IRecipeRegistration registration)
    {
        initDevices ();
        devices.forEach (device -> registration.addRecipes (getRecipesOfTypes (device), device.uid));
    }

    @SuppressWarnings("unchecked")
    private Collection<IRecipe<?>> getRecipesOfTypes (LOTRDevice device)
    {
        Collection<IRecipe<?>> recipes = new ArrayList<> ();
        if (getRecipesMethod != null)
        {
            try
            {
                Minecraft minecraft = Minecraft.getInstance ();

                for (IRecipeType<?> type : device.recipeTypes)
                {
                    recipes.addAll ( ((Map<ResourceLocation, IRecipe<?>>) getRecipesMethod.invoke (
                        minecraft.world.getRecipeManager (),
                        type)).values ().stream ().filter (device::isRecipeValid).collect (Collectors.toList ()));
                }
            }
            catch (Exception e)
            {
                JEILotr.LOGGER.error ("Couldn't get the recipes of the specified type", e);
            }
        }
        return recipes;
    }

    @Override
    public void registerGuiHandlers (IGuiHandlerRegistration registration)
    {
        initDevices ();

        registration.addRecipeClickArea (KegScreen.class, 71, 48, 28, 23, new ResourceLocation ("lotr", "keg"));

        registration.addGuiContainerHandler (LOTRGuiCrafting.class, new IGuiContainerHandler<LOTRGuiCrafting> ()
        {

            public Collection<IGuiClickableArea> getGuiClickableAreas (LOTRGuiCrafting containerScreen, double mouseX,
                double mouseY)
            {
                ResourceLocation uid = containerScreen.getContainer ().isStandardCraftingActive ()
                    ? VanillaRecipeCategoryUid.CRAFTING
                    : devicesByBlock.get (containerScreen.getContainer ().getCraftingBlock ()).uid;

                IGuiClickableArea clickableArea = IGuiClickableArea.createBasic (88, 32, 28, 23,
                    uid);
                return Collections.singleton (clickableArea);
            }
        });

        if (theForgeField != null)
        {
            registration.addGuiContainerHandler (AlloyForgeScreen.class, new IGuiContainerHandler<AlloyForgeScreen> ()
            {

                public Collection<IGuiClickableArea> getGuiClickableAreas (AlloyForgeScreen containerScreen,
                    double mouseX,
                    double mouseY)
                {
                    AbstractAlloyForgeTileEntity forge = null;
                    try
                    {
                        forge = (AbstractAlloyForgeTileEntity) theForgeField.get (containerScreen.getContainer ());
                    }
                    catch (Exception e)
                    {
                        JEILotr.LOGGER.error ("Couldn't get the alloy forge tile entity from the container", e);
                    }

                    if (forge != null)
                    {
                        IGuiClickableArea clickableArea = IGuiClickableArea.createBasic (77, 55, 22, 30,
                            devicesByBlock.get (forge.getBlockState ().getBlock ()).uid);
                        return Collections.singleton (clickableArea);
                    }

                    return new HashSet<> ();
                }
            });
        }
    }

    @Override
    public void registerRecipeTransferHandlers (IRecipeTransferRegistration registration)
    {
        initDevices (); // TODO implement

        devices.forEach (device -> device.registerTransferHandlers (registration));
    }

    private class LOTRKeg extends LOTRDevice
    {

        public LOTRKeg (ResourceLocation uid, ItemStack icon, Collection<IRecipeType<?>> recipeTypes)
        {
            super (uid, icon, recipeTypes);
        }

        @Override
        public IRecipeCategory<?> createCategoryInstance (IGuiHelper guiHelper, IModIdHelper modIdHelper)
        {
            return new Keg (this.uid, this.icon, guiHelper);
        }

        @Override
        public void registerTransferHandlers (IRecipeTransferRegistration registration)
        {
            super.registerTransferHandlers (registration);

            registration.addRecipeTransferHandler (KegContainer.class,
                this.uid, 0, 9, 10, 36);
        }

    }

    private class LOTRAlloyForge extends LOTRDevice
    {

        protected final AbstractAlloyForgeTileEntity forge;

        @SuppressWarnings("resource")
        public LOTRAlloyForge (ResourceLocation uid, ItemStack icon, Collection<IRecipeType<?>> recipeTypes,
            AbstractAlloyForgeTileEntity forge)
        {
            super (uid, icon, recipeTypes);
            this.forge = forge;
            this.forge.setWorldAndPos (Minecraft.getInstance ().world, BlockPos.ZERO);
        }

        @Override
        public IRecipeCategory<?> createCategoryInstance (IGuiHelper guiHelper, IModIdHelper modIdHelper)
        {
            return new AlloyForge (this.uid, this.icon, guiHelper);
        }

        @Override
        public boolean isRecipeValid (IRecipe<?> recipe)
        {
            if (recipe instanceof AbstractAlloyForgeRecipe)
            {
                AbstractAlloyForgeRecipe alloyRecipe = (AbstractAlloyForgeRecipe) recipe;
                return isRecipeValid (alloyRecipe.getIngredients ().get (0), alloyRecipe.getIngredients ().get (1),
                    (alloy, ingred) ->
                    {
                        Inventory testInv = new Inventory (2);
                        testInv.setInventorySlotContents (0, ingred);
                        testInv.setInventorySlotContents (1, alloy);
                        return alloyRecipe.getCraftingResult (testInv);
                    });
            }
            else if (recipe instanceof AbstractCookingRecipe)
            {
                AbstractCookingRecipe cookingRecipe = (AbstractCookingRecipe) recipe;
                return isRecipeValid (cookingRecipe.getIngredients ().get (0), Ingredient.EMPTY,
                    (alloy, ingred) ->
                    {
                        Inventory testInv = new Inventory (1);
                        testInv.setInventorySlotContents (0, ingred);
                        return cookingRecipe.getCraftingResult (testInv);
                    });
            }

            return true;
        }

        private boolean isRecipeValid (Ingredient ingredientItem, Ingredient alloyItem,
            BiFunction<ItemStack, ItemStack, ItemStack> resultFunction)
        {
            ItemStack[] ingredientStacks = ingredientItem.getMatchingStacks ();
            ItemStack[] alloyStacks = alloyItem == Ingredient.EMPTY ? new ItemStack[]
            {ItemStack.EMPTY} : alloyItem.getMatchingStacks ();

            for (ItemStack ingredient : ingredientStacks)
            {
                for (ItemStack alloy : alloyStacks)
                {
                    ItemStack result = forge.getSmeltingResult (ingredient, alloy);
                    if (result == ItemStack.EMPTY || !result.isItemEqual (resultFunction.apply (ingredient, alloy)))
                        return false; // Invalid recipe
                }
            }

            return true;
        }

        @Override
        public void registerTransferHandlers (IRecipeTransferRegistration registration)
        {
            super.registerTransferHandlers (registration);

            registration.addRecipeTransferHandler (AlloyForgeContainer.class, this.uid, 0, 8, 13, 36);
        }

    }

    private class LOTRCraftingTable extends LOTRDevice
    {

        private final RegistryObject<ContainerType<LOTRContainerCraftingTable>> container;

        public LOTRCraftingTable (ResourceLocation uid, ItemStack icon, Collection<IRecipeType<?>> recipeTypes,
            RegistryObject<ContainerType<LOTRContainerCraftingTable>> container)
        {
            super (uid, icon, recipeTypes);
            this.container = container;
        }

        @Override
        public IRecipeCategory<?> createCategoryInstance (IGuiHelper guiHelper, IModIdHelper modIdHelper)
        {
            return new FactionCraftingTable (this.uid, this.icon, guiHelper, modIdHelper);
        }

        @SuppressWarnings("resource")
        @Override
        public void registerTransferHandlers (IRecipeTransferRegistration registration)
        {
            super.registerTransferHandlers (registration);

            registration.addRecipeTransferHandler (
                container.get ().create (0, Minecraft.getInstance ().player.inventory).getClass (),
                this.uid, 1, 9, 10, 36);
            registration.addRecipeTransferHandler (
                container.get ().create (0, Minecraft.getInstance ().player.inventory).getClass (),
                VanillaRecipeCategoryUid.CRAFTING, 1, 9, 10, 36);
        }

    }

    private abstract class LOTRDevice
    {
        protected ResourceLocation uid;
        protected ItemStack icon;
        protected Collection<IRecipeType<?>> recipeTypes;
        protected Class<?> guiClass;
        protected Vector4f guiHandlerArea;

        public LOTRDevice (ResourceLocation uid, ItemStack icon, Collection<IRecipeType<?>> recipeTypes)
        {
            this.uid = uid;
            this.icon = icon;
            this.recipeTypes = recipeTypes;
        }

        public abstract IRecipeCategory<?> createCategoryInstance (IGuiHelper guiHelper, IModIdHelper modIdHelper);

        public boolean isRecipeValid (IRecipe<?> recipe)
        {
            return true;
        }

        public void registerTransferHandlers (IRecipeTransferRegistration registration)
        {

        }

        @Override
        public int hashCode ()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType ().hashCode ();
            result = prime * result + (icon == null ? 0 : icon.hashCode ());
            result = prime * result + (guiClass == null ? 0 : guiClass.hashCode ());
            result = prime * result + (guiHandlerArea == null ? 0 : guiHandlerArea.hashCode ());
            result = prime * result + (recipeTypes == null ? 0 : recipeTypes.hashCode ());
            result = prime * result + (uid == null ? 0 : uid.hashCode ());
            return result;
        }

        @Override
        public boolean equals (Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass () != obj.getClass ())
                return false;
            LOTRDevice other = (LOTRDevice) obj;
            if (!getOuterType ().equals (other.getOuterType ()))
                return false;
            if (icon == null)
            {
                if (other.icon != null)
                    return false;
            }
            else if (!icon.equals (other.icon))
                return false;
            if (guiClass == null)
            {
                if (other.guiClass != null)
                    return false;
            }
            else if (!guiClass.equals (other.guiClass))
                return false;
            if (guiHandlerArea == null)
            {
                if (other.guiHandlerArea != null)
                    return false;
            }
            else if (!guiHandlerArea.equals (other.guiHandlerArea))
                return false;
            if (recipeTypes == null)
            {
                if (other.recipeTypes != null)
                    return false;
            }
            else if (!recipeTypes.equals (other.recipeTypes))
                return false;
            if (uid == null)
            {
                if (other.uid != null)
                    return false;
            }
            else if (!uid.equals (other.uid))
                return false;
            return true;
        }

        private JEIPlugin getOuterType ()
        {
            return JEIPlugin.this;
        }

    }

}
