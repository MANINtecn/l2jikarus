package net.sf.l2jdev.gameserver.network.clientpackets.olympiad;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.olympiad.ExOlympiadRecord;

public class OlympiadUI extends ClientPacket
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
			player.sendPacket(new ExOlympiadRecord(player, this._gameRuleType));
		}
	}
}
