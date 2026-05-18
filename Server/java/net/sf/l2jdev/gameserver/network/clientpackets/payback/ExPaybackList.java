package net.sf.l2jdev.gameserver.network.clientpackets.payback;

import java.util.Map;

import net.sf.l2jdev.gameserver.managers.events.PaybackManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.payback.PaybackList;

public class ExPaybackList extends ClientPacket
{
	private byte _eventId;

	@Override
	protected void readImpl()
	{
		this._eventId = this.readByte();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			PaybackManager manager = PaybackManager.getInstance();
			Map<String, String> local = manager.getLocalString(player.getLang() == null ? "en" : player.getLang());
			if (player.getLevel() < manager.getMinLevel())
			{
				player.sendPacket(new ExShowScreenMessage(local.get("minLevel"), 2000));
			}
			else if (manager.getMaxLevel() != -1 && player.getLevel() > manager.getMaxLevel())
			{
				player.sendPacket(new ExShowScreenMessage(local.get("maxLevel"), 2000));
			}
			else
			{
				player.sendPacket(new PaybackList(player, this._eventId));
			}
		}
	}
}
