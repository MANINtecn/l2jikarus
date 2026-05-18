package net.sf.l2jdev.gameserver.network.clientpackets.gacha;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.gacha.UniqueGachaOpen;

public class ExUniqueGachaOpen extends ClientPacket
{
	private int _fullInfo;
	private int _openMode;

	@Override
	protected void readImpl()
	{
		this._fullInfo = this.readByte();
		this._openMode = this.readByte();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.sendPacket(new UniqueGachaOpen(player, this._fullInfo, this._openMode));
		}
	}
}
