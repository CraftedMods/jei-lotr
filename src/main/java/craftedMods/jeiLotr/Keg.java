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

import java.util.*;

import lotr.common.item.VesselDrinkItem;
import lotr.common.item.VesselDrinkItem.Potency;
import lotr.common.recipe.DrinkBrewingRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.config.Constants;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

public class Keg implements IRecipeCategory<DrinkBrewingRecipe>
{

    private final ResourceLocation uid;
    private final ItemStack forgeIcon;
    private final IDrawable icon;
    private final IDrawable background;

    public Keg (ResourceLocation uid, ItemStack forgeIcon, IGuiHelper guiHelper)
    {
        this.uid = uid;
        this.forgeIcon = forgeIcon;
        icon = guiHelper.createDrawableIngredient (forgeIcon);
        background = guiHelper.createDrawable (Constants.RECIPE_GUI_VANILLA, 0, 60, 116, 54);
    }

    @Override
    public ResourceLocation getUid ()
    {
        return uid;
    }

    @Override
    public String getTitle ()
    {
        return I18n.format (forgeIcon.getTranslationKey ());
    }

    @Override
    public IDrawable getIcon ()
    {
        return icon;
    }

    @Override
    public IDrawable getBackground ()
    {
        return background;
    }

    @Override
    public Class<? extends DrinkBrewingRecipe> getRecipeClass ()
    {
        return DrinkBrewingRecipe.class;
    }

    @Override
    public void setIngredients (DrinkBrewingRecipe recipe, IIngredients ingreds)
    {
        List<Ingredient> ingredients = new ArrayList<> (recipe.getIngredients ());
        ingredients.addAll (Arrays.asList (Ingredient.fromStacks (new ItemStack (Items.WATER_BUCKET)),
            Ingredient.fromStacks (new ItemStack (Items.WATER_BUCKET)),
            Ingredient.fromStacks (new ItemStack (Items.WATER_BUCKET))));
        ingreds.setInputIngredients (ingredients);
        ingreds.setOutput (VanillaTypes.ITEM, recipe.getRecipeOutput ());
    }

    @Override
    public void setRecipe (IRecipeLayout layout, DrinkBrewingRecipe recipe, IIngredients ingreds)
    {
        IGuiItemStackGroup guiItemStacks = layout.getItemStacks ();

        for (int i = 0; i < ingreds.getInputs (VanillaTypes.ITEM).size (); i++)
        {
            guiItemStacks.init (i, true, 18 * (i % 3), 18 * (int) Math.floor (i / 3));
            guiItemStacks.set (i, ingreds.getInputs (VanillaTypes.ITEM).get (i));
        }

        ItemStack resultItem = ingreds.getOutputs (VanillaTypes.ITEM).get (0).get (0);

        List<ItemStack> resultItems = new ArrayList<> ();

        if (resultItem.getItem () instanceof VesselDrinkItem)
        {
            guiItemStacks.init (9, false, 94, 18);

            for (Potency potency : Potency.values ())
            {
                ItemStack newStack = resultItem.copy ();
                VesselDrinkItem.setPotency (newStack, potency);
                resultItems.add (newStack);
            }
        }
        else
        {
            resultItems.add (resultItem);
        }

        guiItemStacks.set (9, resultItems);
    }

    @Override
    public void draw (DrinkBrewingRecipe recipe, double mouseX, double mouseY)
    {
        Minecraft minecraft = Minecraft.getInstance ();
        FontRenderer fontRenderer = minecraft.fontRenderer;

        float experience = recipe.getExperience ();

        if (experience > 0.0f)
        {
            String experienceString = Translator.translateToLocalFormatted (
                "gui.jei.category.smelting.experience",
                new Object[]
                {Float.valueOf (experience)});

            int stringWidth = fontRenderer.getStringWidth (experienceString);
            fontRenderer.drawString (experienceString, background.getWidth () - stringWidth, 0.0f,
                -8355712);
        }

        String brewingTimeString = String.format ("%.1f min", recipe.getBrewTime () / 1200.0f);

        int stringWidth = fontRenderer.getStringWidth (brewingTimeString);
        fontRenderer.drawString (brewingTimeString,
            background.getWidth () - stringWidth, 45.0f,
            -8355712);

    }

}
