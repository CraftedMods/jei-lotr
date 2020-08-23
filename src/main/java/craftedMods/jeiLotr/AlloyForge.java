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

import lotr.common.recipe.AbstractAlloyForgeRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.*;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.plugins.vanilla.cooking.FurnaceVariantCategory;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.util.ResourceLocation;

public class AlloyForge extends FurnaceVariantCategory<IRecipe<?>>
{

    private final ResourceLocation uid;
    private final ItemStack forgeIcon;
    private final IDrawable icon;
    private final IDrawable background;
    private final IDrawable arrow;

    public static final ResourceLocation ALLOY_FORGE_GUI_LOCATION = new ResourceLocation (
        "lotr:textures/gui/alloy_forge.png");

    public AlloyForge (ResourceLocation uid, ItemStack forgeIcon, IGuiHelper guiHelper)
    {
        super (guiHelper);
        this.uid = uid;
        this.forgeIcon = forgeIcon;
        icon = guiHelper.createDrawableIngredient (forgeIcon);
        background = guiHelper.createDrawable (ALLOY_FORGE_GUI_LOCATION, 45, 20, 85, 130);
        arrow = guiHelper
            .drawableBuilder (ALLOY_FORGE_GUI_LOCATION, 176, 14, 16, 30)
            .buildAnimated (200, IDrawableAnimated.StartDirection.TOP, false);
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
    @SuppressWarnings("unchecked")
    public Class<? extends IRecipe<?>> getRecipeClass ()
    {
        return (Class<? extends IRecipe<?>>) (Class<?>) IRecipe.class;
    }

    @Override
    public void setIngredients (IRecipe<?> recipe, IIngredients ingreds)
    {
        ingreds.setInputIngredients (recipe.getIngredients ());
        ingreds.setOutput (VanillaTypes.ITEM, recipe.getRecipeOutput ());
    }

    @Override
    public void setRecipe (IRecipeLayout layout, IRecipe<?> recipe, IIngredients ingreds)
    {
        IGuiItemStackGroup guiItemStacks = layout.getItemStacks ();

        for (int i = 0; i < 4; i++)
        {
            guiItemStacks.init (i, true, 7 + 18 * i, 18);
            guiItemStacks.set (i, ingreds.getInputs (VanillaTypes.ITEM).get (0));
        }

        // Alloy items are not required
        if (ingreds.getInputs (VanillaTypes.ITEM).size () > 1)
        {
            for (int i = 0; i < 4; i++)
            {
                guiItemStacks.init (i + 4, true, 7 + 18 * i, 0);
                guiItemStacks.set (i + 4, ingreds.getInputs (VanillaTypes.ITEM).get (1));
            }
        }

        for (int i = 0; i < 4; i++)
        {
            guiItemStacks.init (i + 8, false, 7 + 18 * i, 64);
            guiItemStacks.set (i + 8, ingreds.getOutputs (VanillaTypes.ITEM).get (0));
        }
    }

    @Override
    public void draw (IRecipe<?> recipe, double mouseX, double mouseY)
    {
        animatedFlame.draw (35, 92);
        arrow.draw (35, 38);

        float experience = 0f;

        if (recipe instanceof AbstractCookingRecipe)
        {
            experience = ((AbstractCookingRecipe) recipe).getExperience ();
        }
        else if (recipe instanceof AbstractAlloyForgeRecipe)
        {
            experience = ((AbstractAlloyForgeRecipe) recipe).getExperience ();
        }

        if (experience > 0.0f)
        {
            String experienceString = Translator.translateToLocalFormatted (
                "gui.jei.category.smelting.experience",
                new Object[]
                {Float.valueOf (experience)});
            Minecraft minecraft = Minecraft.getInstance ();
            FontRenderer fontRenderer = minecraft.fontRenderer;
            int stringWidth = fontRenderer.getStringWidth (experienceString);
            fontRenderer.drawString (experienceString, background.getWidth () - stringWidth, 43.0f,
                -8355712);
        }
    }

}
