package net.sf.l2jdev.gameserver.network.clientpackets.newhenna;

import net.sf.l2jdev.gameserver.data.xml.HennaPatternPotentialData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.henna.DyePotentialFee;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.newhenna.NewHennaList;
import net.sf.l2jdev.gameserver.network.serverpackets.newhenna.NewHennaPotenEnchantReset;

public class ExRequestNewHennaEnchantReset extends ClientPacket
{
	private int _resetItemId;

	@Override
	protected void readImpl()
	{
		this._resetItemId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			int dailyReset = player.getDyePotentialDailyEnchantReset();

			ItemHolder enchant;
			try
			{
				enchant = HennaPatternPotentialData.getInstance().getEnchantReset().stream().filter(item -> item.getId() == this._resetItemId).findFirst().get();
			}
			catch (Exception var5)
			{
				return;
			}

			if (player.destroyItemByItemId(ItemProcessType.FEE, enchant.getId(), enchant.getCount(), player, true))
			{
				DyePotentialFee newFee = HennaPatternPotentialData.getInstance().getFee(1);
				player.setDyePotentialDailyCount(newFee.getDailyCount());
				player.setDyePotentialDailyEnchantReset(dailyReset + 1);
				player.sendPacket(new NewHennaPotenEnchantReset(true));
				player.sendPacket(new NewHennaList(player, 1));
			}
			else
			{
				player.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_MONEY_TO_USE_THE_FUNCTION));
			}
		}
	}
}
