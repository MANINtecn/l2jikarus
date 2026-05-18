package net.sf.l2jdev.gameserver.network.clientpackets.olympiad;

import net.sf.l2jdev.gameserver.data.enums.CategoryType;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.olympiad.CompetitionType;
import net.sf.l2jdev.gameserver.model.olympiad.Olympiad;
import net.sf.l2jdev.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.olympiad.ExOlympiadInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.olympiad.ExOlympiadMatchMakingResult;
import net.sf.l2jdev.gameserver.network.serverpackets.olympiad.ExOlympiadRecord;

public class OlympiadMatchMaking extends ClientPacket
{
	private byte _gameRuleType;

	@Override
	protected void readImpl()
	{
		this._gameRuleType = 1;
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (player.isPrisoner())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THIS_FUNCTION_IN_THE_UNDERGROUND_LABYRINTH);
			}
			else if (player.isRegisteredOnEvent())
			{
				player.sendMessage("You cannot register to Olympiad while participating in events!");
			}
			else if (!player.isInTimedHuntingZone() && !player.isInInstance() && !player.isCursedWeaponEquipped() && !player.isInsideZone(ZoneId.PVP) && player.getReputation() >= 0)
			{
				if (Olympiad.getInstance().getMillisToCompEnd() < 600000L)
				{
					player.sendPacket(SystemMessageId.GAME_PARTICIPATION_REQUEST_MUST_BE_FILED_NOT_EARLIER_THAN_10_MIN_AFTER_THE_GAME_ENDS);
				}
				else
				{
					if (this._gameRuleType == 1)
					{
						if (!player.isInCategory(CategoryType.FOURTH_CLASS_GROUP) || player.getLevel() < 75)
						{
							player.sendPacket(SystemMessageId.CHARACTER_S_LEVEL_IS_TOO_LOW);
						}
						else if (!player.isInventoryUnder80(false))
						{
							player.sendPacket(SystemMessageId.UNABLE_TO_PROCESS_THIS_REQUEST_UNTIL_YOUR_INVENTORY_S_WEIGHT_AND_SLOT_COUNT_ARE_LESS_THAN_80_PERCENT_OF_CAPACITY);
						}
						else
						{
							OlympiadManager.getInstance().registerNoble(player, CompetitionType.NON_CLASSED);
						}
					}

					player.sendPacket(new ExOlympiadMatchMakingResult(this._gameRuleType, 1));
					player.sendPacket(new ExOlympiadInfo(2, Olympiad.getInstance().getRemainingTime()));
					player.sendPacket(new ExOlympiadRecord(player, this._gameRuleType));
				}
			}
			else
			{
				player.sendMessage("You cannot register to Olympiad at the moment!");
			}
		}
	}
}
