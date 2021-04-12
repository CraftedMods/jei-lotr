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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lotr.common.tileentity.AbstractAlloyForgeTileEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod("jeilotr")
public class JEILotr {
	private static int ALLOY_FORGE_RECIPE_VERSION = 3;

	public static final Logger LOGGER = LogManager.getLogger();

	public JEILotr() {
		if (FMLEnvironment.dist == Dist.CLIENT) {
			FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

			MinecraftForge.EVENT_BUS.register(this);
		} else {
			LOGGER.warn("JEI LOTR was loaded on a server - it doesn't do anyting there and may cause crashes");
		}
	}

	private void setup(final FMLCommonSetupEvent event) {
		if (ALLOY_FORGE_RECIPE_VERSION != AbstractAlloyForgeTileEntity.RECIPE_FUNCTIONALITY_VERSION_FOR_JEI) {
			LOGGER.warn(
					"The supported alloy forge recipe version differs from the one in the current LOTR Mod version - the alloy forge recipe handlers could show wrong recipes.");
		}
	}

	@SubscribeEvent
	public void login(EntityJoinWorldEvent event) {
		if (event.getEntity() instanceof ClientPlayerEntity) {
			VersionChecker.CheckResult result = VersionChecker
					.getResult(ModList.get().getModContainerById("jeilotr").get().getModInfo());
			if (result != null && (result.status == VersionChecker.Status.OUTDATED
					|| result.status == VersionChecker.Status.BETA_OUTDATED)) {
				((ClientPlayerEntity) event.getEntity()).sendMessage(
						new StringTextComponent(
								"\u00A73[JEI LOTR]:\u00A7r A new version (" + result.target.toString() + ") was found"),
						event.getEntity().getUUID());
			}
		}
	}
}
