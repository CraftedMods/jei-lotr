/*
 * Copyright (C) 2020-2021 CraftedMods (see http://github.com/CraftedMods)
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

import lotr.common.recipe.LOTRShapedRecipe;
import lotr.common.recipe.LOTRShapelessRecipe;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.plugins.vanilla.crafting.CraftingCategoryExtension;
import mezz.jei.plugins.vanilla.crafting.CraftingRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class FactionCraftingTable extends CraftingRecipeCategory {

	private final ResourceLocation uid;
	private final ItemStack ctIcon;
	private final IDrawable icon;
	
	private ITextComponent titleTextComponent;

	public FactionCraftingTable(ResourceLocation uid, ItemStack ctIcon, IGuiHelper guiHelper,
			IModIdHelper modIdHelper) {
		super(guiHelper);

		this.uid = uid;
		this.ctIcon = ctIcon;
		this.icon = guiHelper.createDrawableIngredient(ctIcon);

		this.addCategoryExtension(LOTRShapedRecipe.class, CraftingCategoryExtension::new);
		this.addCategoryExtension(LOTRShapelessRecipe.class, CraftingCategoryExtension::new);
		
		this.titleTextComponent = new TranslationTextComponent(I18n.get(ctIcon.getDescriptionId()));
	}

	@Override
	public ResourceLocation getUid() {
		return uid;
	}

	@Override
	public Class<? extends ICraftingRecipe> getRecipeClass() {
		return ICraftingRecipe.class;
	}

	@Override
	public String getTitle() {
		return I18n.get(ctIcon.getDescriptionId());
	}
	
	@Override
	public ITextComponent getTitleAsTextComponent() {
		return this.titleTextComponent;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

}
