package net.sf.l2jdev.gameserver.network.serverpackets.pledgeV3;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.GlobalVariablesManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPledgeClassicRaidInfo extends ServerPacket
{
	private final Clan _clan;
	private final int _stage;

	public ExPledgeClassicRaidInfo(Player player)
	{
		this._clan = player.getClan();
		this._stage = this._clan == null ? 0 : GlobalVariablesManager.getInstance().getInt("MA_C" + this._clan.getId(), 0);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PLEDGE_CLASSIC_RAID_INFO.writeId(this, buffer);
		if (this._clan == null)
		{
			buffer.writeInt(0);
		}
		else
		{
			buffer.writeInt(this._stage);
			buffer.writeInt(5);

			for (int i = 1; i <= 5; i++)
			{
				buffer.writeInt(1867);
				buffer.writeInt(i);
			}
		}
	}
}
