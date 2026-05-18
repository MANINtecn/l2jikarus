package net.sf.l2jdev.gameserver.network.clientpackets.castlewar;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class ExPledgeMercenaryRecruitInfoSet extends ClientPacket
{
	private boolean _isRecruit;
	private int _mercenaryReaward;

	@Override
	protected void readImpl()
	{
		this.readInt();
		this.readInt();
		this._isRecruit = this.readInt() == 1;
		this._mercenaryReaward = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Clan clan = player.getClan();
			if (clan != null)
			{
				if (!clan.isRecruitMercenary() || clan.getMapMercenary().size() <= 0)
				{
					clan.setRecruitMercenary(this._isRecruit);
					clan.setRewardMercenary(this._mercenaryReaward);
				}
			}
		}
	}
}
