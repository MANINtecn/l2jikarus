package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.olympiad.Hero;

public class RequestWriteHeroWords extends ClientPacket
{
	private String _heroWords;

	@Override
	protected void readImpl()
	{
		this._heroWords = this.readString();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null && player.isHero())
		{
			if (this._heroWords != null && this._heroWords.length() <= 300)
			{
				Hero.getInstance().setHeroMessage(player, this._heroWords);
			}
		}
	}
}
