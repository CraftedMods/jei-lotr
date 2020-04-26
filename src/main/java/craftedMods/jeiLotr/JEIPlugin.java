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

import java.lang.reflect.Method;
import java.util.*;

import lotr.common.init.LOTRBlocks;
import lotr.common.recipe.LOTRRecipes;
import mezz.jei.api.*;
import mezz.jei.api.registration.*;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Vector4f;
import net.minecraft.item.crafting.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;

@JeiPlugin
public class JEIPlugin implements IModPlugin
{
    public static final ResourceLocation DWARVEN_CT_UID = new ResourceLocation ("lotr", "dwarven_ct");
    public static final ResourceLocation GONDOR_CT_UID = new ResourceLocation ("lotr", "gondor_ct");
    public static final ResourceLocation GALADHRIM_CT_UID = new ResourceLocation ("lotr", "galadhrim_ct");
    public static final ResourceLocation HARAD_CT_UID = new ResourceLocation ("lotr", "harad_ct");
    public static final ResourceLocation LINDON_CT_UID = new ResourceLocation ("lotr", "lindon_ct");
    public static final ResourceLocation MORDOR_CT_UID = new ResourceLocation ("lotr", "mordor_ct");
    public static final ResourceLocation RIVENDELL_CT_UID = new ResourceLocation ("lotr", "rivendell_ct");
    public static final ResourceLocation ROHAN_CT_UID = new ResourceLocation ("lotr", "rohan_ct");
    public static final ResourceLocation UMBAR_CT_UID = new ResourceLocation ("lotr", "umbar_ct");
    public static final ResourceLocation WOOD_ELVEN_CT_UID = new ResourceLocation ("lotr", "wood_elven_ct");

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
                getRecipesMethod = RecipeManager.class.getDeclaredMethod ("func_215370_b", IRecipeType.class);
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

            craftingTables.add (new LOTRCraftingTable (DWARVEN_CT_UID,
                getCTBlock (LOTRBlocks.DWARVEN_CRAFTING_TABLE), LOTRRecipes.DWARVEN_CRAFTING));
            craftingTables.add (new LOTRCraftingTable (GONDOR_CT_UID,
                getCTBlock (LOTRBlocks.GONDOR_CRAFTING_TABLE), LOTRRecipes.GONDOR_CRAFTING));
            craftingTables.add (new LOTRCraftingTable (GALADHRIM_CT_UID,
                getCTBlock (LOTRBlocks.GALADHRIM_CRAFTING_TABLE), LOTRRecipes.GALADHRIM_CRAFTING));
            craftingTables.add (new LOTRCraftingTable (HARAD_CT_UID,
                getCTBlock (LOTRBlocks.HARAD_CRAFTING_TABLE), LOTRRecipes.HARAD_CRAFTING));
            craftingTables.add (new LOTRCraftingTable (LINDON_CT_UID,
                getCTBlock (LOTRBlocks.LINDON_CRAFTING_TABLE), LOTRRecipes.LINDON_CRAFTING));
            craftingTables.add (new LOTRCraftingTable (MORDOR_CT_UID,
                getCTBlock (LOTRBlocks.MORDOR_CRAFTING_TABLE), LOTRRecipes.MORDOR_CRAFTING));
            craftingTables.add (new LOTRCraftingTable (RIVENDELL_CT_UID,
                getCTBlock (LOTRBlocks.RIVENDELL_CRAFTING_TABLE), LOTRRecipes.RIVENDELL_CRAFTING));
            craftingTables.add (new LOTRCraftingTable (ROHAN_CT_UID,
                getCTBlock (LOTRBlocks.ROHAN_CRAFTING_TABLE), LOTRRecipes.ROHAN_CRAFTING));
            craftingTables.add (new LOTRCraftingTable (UMBAR_CT_UID,
                getCTBlock (LOTRBlocks.UMBAR_CRAFTING_TABLE), LOTRRecipes.UMBAR_CRAFTING));
            craftingTables.add (new LOTRCraftingTable (WOOD_ELVEN_CT_UID,
                getCTBlock (LOTRBlocks.WOOD_ELVEN_CRAFTING_TABLE), LOTRRecipes.WOOD_ELVEN_CRAFTING));
        }
    }

    private Block getCTBlock (RegistryObject<Block> ctBlockRegistryObject)
    {
        return ctBlockRegistryObject.orElseGet ( () -> Blocks.CRAFTING_TABLE);
    }

    @Override
    public void registerRecipeCatalysts (IRecipeCatalystRegistration registration)
    {
        initCraftingTables ();
        craftingTables.forEach (table -> registration.addRecipeCatalyst (table.ctBlock, table.uid));
    }

    @Override
    public void registerCategories (IRecipeCategoryRegistration registration)
    {
        initCraftingTables ();
        craftingTables.forEach (table ->
        {
            registration
                .addRecipeCategories (
                    new FactionCraftingTable (table.uid, table.ctBlock, registration.getJeiHelpers ().getGuiHelper (),
                        registration.getJeiHelpers ().getModIdHelper ()));
        });
    }

    @Override
    public void registerRecipes (IRecipeRegistration registration)
    {
        initCraftingTables ();
        craftingTables.forEach (table -> registration.addRecipes (getRecipesOfType (table.recipeType), table.uid));

    }

    @SuppressWarnings("unchecked")
    private Collection<IRecipe<?>> getRecipesOfType (IRecipeType<?> type)
    {
        if (getRecipesMethod != null)
        {
            try
            {
                return ((Map<ResourceLocation, IRecipe<?>>) getRecipesMethod.invoke (
                    Minecraft.getInstance ().world.getRecipeManager (),
                    type)).values ();
            }
            catch (Exception e)
            {
                JEILotr.LOGGER.error ("Couldn't get the recipes of the specified type", e);
            }
        }
        return Arrays.asList ();
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
        private Block ctBlock;
        private IRecipeType<?> recipeType;
        private Class<?> guiClass;
        private Vector4f guiHandlerArea;

        public LOTRCraftingTable (ResourceLocation uid, Block ctBlock, IRecipeType<?> recipeType)
        {
            this.uid = uid;
            this.ctBlock = ctBlock;
            this.recipeType = recipeType;
        }

        @Override
        public int hashCode ()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType ().hashCode ();
            result = prime * result + (ctBlock == null ? 0 : ctBlock.hashCode ());
            result = prime * result + (guiClass == null ? 0 : guiClass.hashCode ());
            result = prime * result + (guiHandlerArea == null ? 0 : guiHandlerArea.hashCode ());
            result = prime * result + (recipeType == null ? 0 : recipeType.hashCode ());
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
            if (ctBlock == null)
            {
                if (other.ctBlock != null)
                    return false;
            }
            else if (!ctBlock.equals (other.ctBlock))
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
            if (recipeType == null)
            {
                if (other.recipeType != null)
                    return false;
            }
            else if (!recipeType.equals (other.recipeType))
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
