package net.sf.l2jdev.gameserver.network.clientpackets.balthusevent;

import net.sf.l2jdev.gameserver.managers.events.BalthusEventManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.balthusevent.ExBalthusEvent;

public class RequestEventBalthusToken extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			int count = player.getVariables().getInt("BALTHUS_REWARD", 0);
			if (count != 0)
			{
				if (player.addItem(ItemProcessType.COMPENSATE, BalthusEventManager.getInstance().getConsolation().getId(), count, player, true) != null)
				{
					player.getVariables().set("BALTHUS_REWARD", 0);
					player.sendPacket(new ExBalthusEvent(player));
				}
			}
			else
			{
				player.sendPacket(SystemMessageId.NO_SIBI_S_COINS_AVAILABLE);
			}
		}
	}
}
