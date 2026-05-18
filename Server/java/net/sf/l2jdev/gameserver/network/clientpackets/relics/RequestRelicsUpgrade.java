package net.sf.l2jdev.gameserver.network.clientpackets.relics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.config.RelicSystemConfig;
import net.sf.l2jdev.gameserver.data.holders.RelicDataHolder;
import net.sf.l2jdev.gameserver.data.xml.RelicData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.player.PlayerRelicData;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.relics.ExRelicsCollectionUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.relics.ExRelicsList;
import net.sf.l2jdev.gameserver.network.serverpackets.relics.ExRelicsUpdateList;
import net.sf.l2jdev.gameserver.network.serverpackets.relics.ExRelicsUpgrade;

public class RequestRelicsUpgrade extends ClientPacket
{
	private int _relicId;
	private int _relicLevel;
	private final List<Integer> _ingredients = new ArrayList<>();
	private int _chance = 0;

	@Override
	protected void readImpl()
	{
		this._relicId = this.readInt();
		this._relicLevel = this.readInt();
		int ingredientCount = this.readInt();

		for (int i = 0; i < ingredientCount; i++)
		{
			this._ingredients.add(this.readInt());
		}

		this._chance = RelicData.getInstance().getEnchantRateByIngredientCount(ingredientCount);
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			ItemHolder itemFee = RelicData.getInstance().getEnchantFee();
			if (!player.destroyItemByItemId(ItemProcessType.FEE, itemFee.getId(), itemFee.getCount(), player, true))
			{
				player.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ADENA));
			}
			else
			{
				boolean success = Rnd.get(100) < this._chance;
				Collection<PlayerRelicData> storedRelics = player.getRelics();
				PlayerRelicData existingRelic = null;

				for (PlayerRelicData relic : storedRelics)
				{
					if (relic.getRelicId() == this._relicId && relic.getRelicIndex() < 300)
					{
						existingRelic = relic;
						break;
					}
				}

				if (existingRelic != null && existingRelic.getRelicLevel() < 4)
				{
					existingRelic.setRelicLevel(success ? existingRelic.getRelicLevel() + 1 : existingRelic.getRelicLevel());
					this._relicLevel = existingRelic.getRelicLevel();
					if (RelicSystemConfig.RELIC_SYSTEM_DEBUG_ENABLED)
					{
						player.sendMessage("Relic Id: " + existingRelic.getRelicId() + " " + (success ? "Upgrade successful! Relic is now level: " : "Upgrade failed! Relic is still level: ") + this._relicLevel);
					}

					player.sendPacket(new ExRelicsUpdateList(1, existingRelic.getRelicId(), existingRelic.getRelicLevel(), existingRelic.getRelicCount()));
					if (!player.isRelicRegistered(existingRelic.getRelicId(), existingRelic.getRelicLevel()))
					{
						player.sendPacket(new ExRelicsCollectionUpdate(player, existingRelic.getRelicId(), existingRelic.getRelicLevel()));
					}
				}

				for (int ingredient : this._ingredients)
				{
					PlayerRelicData ingredientRelic = null;

					for (PlayerRelicData relicx : storedRelics)
					{
						if (relicx.getRelicId() == ingredient)
						{
							ingredientRelic = relicx;
							break;
						}
					}

					if (ingredientRelic != null && ingredientRelic.getRelicCount() > 0)
					{
						ingredientRelic.setRelicCount(ingredientRelic.getRelicCount() - 1);
						if (RelicSystemConfig.RELIC_SYSTEM_DEBUG_ENABLED)
						{
							player.sendMessage("Ingredient Relic data updated, ID: " + ingredientRelic + ", Count: " + ingredientRelic.getRelicCount());
						}
					}
				}

				player.storeRelics();
				RelicDataHolder relicHolder = RelicData.getInstance().getRelic(this._relicId);
				player.giveRelicSkill(relicHolder, this._relicLevel);
				player.sendPacket(new ExRelicsList(player));
				player.sendPacket(new ExRelicsUpgrade(success, this._relicId, this._relicLevel));
			}
		}
	}
}
