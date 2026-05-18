package net.sf.l2jdev.gameserver.network.clientpackets.huntpass;

import java.util.Calendar;

import net.sf.l2jdev.gameserver.config.HuntPassConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.huntpass.HuntPassInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.huntpass.HuntPassSayhasSupportInfo;

public class RequestHuntPassBuyPremium extends ClientPacket
{
	private int _huntPassType;

	@Override
	protected void readImpl()
	{
		this._huntPassType = this.readByte();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Calendar calendar = Calendar.getInstance();
			if (calendar.get(5) == HuntPassConfig.HUNT_PASS_PERIOD && calendar.get(11) == 6 && calendar.get(12) < 30)
			{
				player.sendPacket(SystemMessageId.CURRENTLY_UNAVAILABLE_FOR_PURCHASE_YOU_CAN_BUY_THE_SEASON_PASS_ADDITIONAL_REWARDS_ONLY_UNTIL_6_30_A_M_OF_THE_SEASON_S_LAST_DAY);
			}
			else if (!player.destroyItemByItemId(ItemProcessType.FEE, HuntPassConfig.HUNT_PASS_PREMIUM_ITEM_ID, HuntPassConfig.HUNT_PASS_PREMIUM_ITEM_COUNT, player, true))
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_MONEY_TO_USE_THE_FUNCTION);
			}
			else
			{
				player.getHuntPass().setPremium(true);
				player.sendPacket(new HuntPassSayhasSupportInfo(player));
				player.sendPacket(new HuntPassInfo(player, this._huntPassType));
			}
		}
	}
}
