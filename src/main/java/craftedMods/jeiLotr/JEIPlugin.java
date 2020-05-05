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

import lotr.common.recipe.*;
import mezz.jei.api.*;
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

    private Collection<LOTRCraftingTable> craftingTables;

    @Override
    public ResourceLocation getPluginUid ()
    {
        return new ResourceLocation ("crafted_mods", "jei_lotr");
    }

    public void initCraftingTables ()
    {
        if (craftingTables == null)
        {
            craftingTables = new HashSet<> ();

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

                        craftingTables
                            .add (new LOTRCraftingTable (new ResourceLocation ("lotr", type.recipeID.split (":")[1]),
                                type.getIcon (), types));
                    }
                }
                JEILotr.LOGGER.debug ("Found " + craftingTables.size () + " faction crafting tables");
            }
            catch (Exception e)
            {
                JEILotr.LOGGER
                    .error ("Couldn't instantiate the faction crafting tables from the LOTRRecipes type fields", e);
            }

        }
    }

    @Override
    public void registerRecipeCatalysts (IRecipeCatalystRegistration registration)
    {
        initCraftingTables ();
        craftingTables.forEach (table -> registration.addRecipeCatalyst (table.ctIcon.copy (), table.uid));
    }

    @Override
    public void registerCategories (IRecipeCategoryRegistration registration)
    {
        initCraftingTables ();
        craftingTables.forEach (table ->
        {
            registration
                .addRecipeCategories (
                    new FactionCraftingTable (table.uid, table.ctIcon, registration.getJeiHelpers ().getGuiHelper (),
                        registration.getJeiHelpers ().getModIdHelper ()));
        });
    }

    @Override
    public void registerRecipes (IRecipeRegistration registration)
    {
        initCraftingTables ();
        craftingTables.forEach (table -> registration.addRecipes (getRecipesOfTypes (table.recipeTypes), table.uid));

    }

    @SuppressWarnings("unchecked")
    private Collection<IRecipe<?>> getRecipesOfTypes (Collection<IRecipeType<?>> types)
    {
        Collection<IRecipe<?>> recipes = new ArrayList<> ();
        if (getRecipesMethod != null)
        {
            try
            {
                for (IRecipeType<?> type : types)
                {
                    recipes.addAll ( ((Map<ResourceLocation, IRecipe<?>>) getRecipesMethod.invoke (
                        Minecraft.getInstance ().world.getRecipeManager (),
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
        initCraftingTables (); // TODO implement
    }

    @Override
    public void registerRecipeTransferHandlers (IRecipeTransferRegistration registration)
    {
        initCraftingTables (); // TODO implement

    }

    private class LOTRCraftingTable
    {
        private ResourceLocation uid;
        private ItemStack ctIcon;
        private Collection<IRecipeType<?>> recipeTypes;
        private Class<?> guiClass;
        private Vector4f guiHandlerArea;

        public LOTRCraftingTable (ResourceLocation uid, ItemStack ctIcon, Collection<IRecipeType<?>> recipeTypes)
        {
            this.uid = uid;
            this.ctIcon = ctIcon;
            this.recipeTypes = recipeTypes;
        }

        @Override
        public int hashCode ()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType ().hashCode ();
            result = prime * result + (ctIcon == null ? 0 : ctIcon.hashCode ());
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
            LOTRCraftingTable other = (LOTRCraftingTable) obj;
            if (!getOuterType ().equals (other.getOuterType ()))
                return false;
            if (ctIcon == null)
            {
                if (other.ctIcon != null)
                    return false;
            }
            else if (!ctIcon.equals (other.ctIcon))
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
