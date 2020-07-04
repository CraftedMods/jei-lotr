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

import lotr.common.init.LOTRBlocks;
import lotr.common.recipe.*;
import mezz.jei.api.*;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.helpers.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Vector4f;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.util.ResourceLocation;

@JeiPlugin
public class JEIPlugin implements IModPlugin
{
    private static Method getRecipesMethod;

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
    }

    private Collection<LOTRDevice> devices;

    @Override
    public ResourceLocation getPluginUid ()
    {
        return new ResourceLocation ("crafted_mods", "jei_lotr");
    }

    public void initDevices ()
    {
        if (devices == null)
        {
            devices = new HashSet<> ();

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

                        devices
                            .add (new LOTRCraftingTable (new ResourceLocation ("lotr", type.recipeID.split (":")[1]),
                                type.getIcon (), types));
                    }
                }
                JEILotr.LOGGER.debug ("Found " + devices.size () + " faction crafting tables");
            }
            catch (Exception e)
            {
                JEILotr.LOGGER
                    .error ("Couldn't instantiate the faction crafting tables from the LOTRRecipes type fields", e);
            }

            devices.add (new LOTRAlloyForge (new ResourceLocation ("lotr", "dwarven_forge"),
                new ItemStack (LOTRBlocks.DWARVEN_FORGE.get ()),
                Arrays.asList (LOTRRecipes.DWARVEN_FORGE, LOTRRecipes.ALLOY_FORGE, LOTRRecipes.DWARVEN_FORGE_ALLOY)));
            devices.add (new LOTRAlloyForge (new ResourceLocation ("lotr", "orc_forge"),
                new ItemStack (LOTRBlocks.ORC_FORGE.get ()),
                Arrays.asList (LOTRRecipes.ORC_FORGE, LOTRRecipes.ALLOY_FORGE, LOTRRecipes.ORC_FORGE_ALLOY)));
            devices.add (new LOTRAlloyForge (new ResourceLocation ("lotr", "elven_forge"),
                new ItemStack (LOTRBlocks.ELVEN_FORGE.get ()),
                Arrays.asList (LOTRRecipes.ELVEN_FORGE, LOTRRecipes.ALLOY_FORGE, LOTRRecipes.ELVEN_FORGE_ALLOY)));
            devices.add (new LOTRAlloyForge (new ResourceLocation ("lotr", "alloy_forge"),
                new ItemStack (LOTRBlocks.ALLOY_FORGE.get ()),
                Arrays.asList (LOTRRecipes.ALLOY_FORGE)));

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
        devices.forEach (device -> registration.addRecipes (getRecipesOfTypes (device.recipeTypes), device.uid));
    }

    @SuppressWarnings("unchecked")
    private Collection<IRecipe<?>> getRecipesOfTypes (Collection<IRecipeType<?>> types)
    {
        Collection<IRecipe<?>> recipes = new ArrayList<> ();
        if (getRecipesMethod != null)
        {
            try
            {
                Minecraft minecraft = Minecraft.getInstance ();

                for (IRecipeType<?> type : types)
                {
                    recipes.addAll ( ((Map<ResourceLocation, IRecipe<?>>) getRecipesMethod.invoke (
                        minecraft.world.getRecipeManager (),
                        type)).values ());
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
        initDevices (); // TODO implement
    }

    @Override
    public void registerRecipeTransferHandlers (IRecipeTransferRegistration registration)
    {
        initDevices (); // TODO implement
    }

    private class LOTRAlloyForge extends LOTRDevice
    {

        public LOTRAlloyForge (ResourceLocation uid, ItemStack icon, Collection<IRecipeType<?>> recipeTypes)
        {
            super (uid, icon, recipeTypes);
        }

        @Override
        public IRecipeCategory<?> createCategoryInstance (IGuiHelper guiHelper, IModIdHelper modIdHelper)
        {
            return new AlloyForge (this.uid, this.icon, guiHelper);
        }

    }

    private class LOTRCraftingTable extends LOTRDevice
    {

        public LOTRCraftingTable (ResourceLocation uid, ItemStack icon, Collection<IRecipeType<?>> recipeTypes)
        {
            super (uid, icon, recipeTypes);
        }

        @Override
        public IRecipeCategory<?> createCategoryInstance (IGuiHelper guiHelper, IModIdHelper modIdHelper)
        {
            return new FactionCraftingTable (this.uid, this.icon, guiHelper, modIdHelper);
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
