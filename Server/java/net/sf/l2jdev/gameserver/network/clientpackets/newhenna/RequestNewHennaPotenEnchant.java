package net.sf.l2jdev.gameserver.network.clientpackets.newhenna;

import java.util.Map.Entry;
import java.util.Optional;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.data.xml.HennaPatternPotentialData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerHennaEnchant;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.henna.DyePotentialFee;
import net.sf.l2jdev.gameserver.model.item.henna.HennaPoten;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.newhenna.NewHennaPotenEnchant;

public class RequestNewHennaPotenEnchant extends ClientPacket
{
	private int _slotId;
	private int _costItemId;

	@Override
	protected void readImpl()
	{
		this._slotId = this.readByte();
		this._costItemId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			int dailyStep = player.getDyePotentialDailyStep();
			DyePotentialFee currentFee = HennaPatternPotentialData.getInstance().getFee(dailyStep);
			int dailyCount = player.getDyePotentialDailyCount();
			if (this._slotId >= 1 && this._slotId <= 4)
			{
				HennaPoten hennaPattern = player.getHennaPoten(this._slotId);
				int enchantExp = hennaPattern.getEnchantExp();
				int fullExpNeeded = HennaPatternPotentialData.getInstance().getExpForLevel(hennaPattern.getEnchantLevel());
				if (enchantExp >= fullExpNeeded && hennaPattern.getEnchantLevel() == 30)
				{
					player.sendPacket(new NewHennaPotenEnchant(this._slotId, hennaPattern.getEnchantLevel(), hennaPattern.getEnchantExp(), dailyStep, dailyCount, hennaPattern.getActiveStep(), true));
				}
				else if (currentFee != null && dailyCount > 0)
				{
					Optional<ItemHolder> itemFee = currentFee.getItems().stream().filter(ih -> ih.getId() == this._costItemId).findAny();
					if (!itemFee.isEmpty() && player.destroyItemByItemId(ItemProcessType.FEE, itemFee.get().getId(), itemFee.get().getCount(), player, true))
					{
						long adenaFee = currentFee.getAdenaFee();
						if (adenaFee <= 0L || player.destroyItemByItemId(ItemProcessType.FEE, 57, adenaFee, player, true))
						{
							if (--dailyCount <= 0 && dailyStep != HennaPatternPotentialData.getInstance().getMaxPotenEnchantStep())
							{
								DyePotentialFee newFee = HennaPatternPotentialData.getInstance().getFee(++dailyStep);
								if (newFee != null)
								{
									dailyCount = 0;
								}

								player.setDyePotentialDailyCount(dailyCount);
							}
							else
							{
								player.setDyePotentialDailyCount(dailyCount);
							}

							double totalChance = 0.0;
							double random = Rnd.nextDouble() * 100.0;

							for (Entry<Integer, Double> entry : currentFee.getEnchantExp().entrySet())
							{
								totalChance += entry.getValue();
								if (random <= totalChance)
								{
									int increase = entry.getKey();
									int newEnchantExp = hennaPattern.getEnchantExp() + increase;
									int PatternExpNeeded = HennaPatternPotentialData.getInstance().getExpForLevel(hennaPattern.getEnchantLevel());
									if (newEnchantExp >= PatternExpNeeded && hennaPattern.getEnchantLevel() < 30)
									{
										newEnchantExp -= PatternExpNeeded;
										if (hennaPattern.getEnchantLevel() < HennaPatternPotentialData.getInstance().getMaxPotenLevel())
										{
											hennaPattern.setEnchantLevel(hennaPattern.getEnchantLevel() + 1);
											player.applyDyePotenSkills();
										}
									}

									hennaPattern.setEnchantExp(newEnchantExp);
									hennaPattern.setSlotPosition(this._slotId);
									player.sendPacket(new NewHennaPotenEnchant(this._slotId, hennaPattern.getEnchantLevel(), hennaPattern.getEnchantExp(), dailyStep, dailyCount, hennaPattern.getActiveStep(), true));
									if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_HENNA_ENCHANT, player))
									{
										EventDispatcher.getInstance().notifyEventAsync(new OnPlayerHennaEnchant(player), player);
									}

									return;
								}
							}
						}
					}
				}
			}
		}
	}
}
