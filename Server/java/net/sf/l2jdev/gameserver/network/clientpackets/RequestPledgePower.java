package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.ClanAccess;
import net.sf.l2jdev.gameserver.network.serverpackets.ManagePledgePower;

public class RequestPledgePower extends ClientPacket
{
	private int _rank;
	private int _action;
	private int _privs;

	@Override
	protected void readImpl()
	{
		this._rank = this.readInt();
		this._action = this.readInt();
		if (this._action == 2)
		{
			this._privs = this.readInt();
		}
		else
		{
			this._privs = 0;
		}
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Clan clan = player.getClan();
			player.sendPacket(new ManagePledgePower(clan, this._action, this._rank));
			if (this._action == 2 && player.isClanLeader())
			{
				if (this._rank == 9)
				{
					this._privs = this._privs & (ClanAccess.ACCESS_WAREHOUSE.getMask() | ClanAccess.HALL_OPEN_DOOR.getMask() | ClanAccess.CASTLE_OPEN_DOOR.getMask());
				}

				clan.setRankPrivs(this._rank, this._privs);
			}
		}
	}
}
