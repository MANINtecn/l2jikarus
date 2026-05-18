package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.data.sql.CrestTable;
import net.sf.l2jdev.gameserver.model.Crest;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.enums.CrestType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class RequestSetAllyCrest extends ClientPacket
{
	private int _length;
	private byte[] _data = null;

	@Override
	protected void readImpl()
	{
		this._length = this.readInt();
		if (this._length <= 192)
		{
			this._data = this.readBytes(this._length);
		}
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this._length < 0)
			{
				player.sendMessage("File transfer error.");
			}
			else if (this._length > 192)
			{
				player.sendPacket(SystemMessageId.PLEASE_ADJUST_THE_IMAGE_SIZE_TO_8X12);
			}
			else if (player.getAllyId() == 0)
			{
				player.sendPacket(SystemMessageId.ACCESS_ONLY_FOR_THE_CHANNEL_FOUNDER);
			}
			else
			{
				Clan leaderClan = ClanTable.getInstance().getClan(player.getAllyId());
				if (player.getClanId() == leaderClan.getId() && player.isClanLeader())
				{
					if (this._length == 0)
					{
						if (leaderClan.getAllyCrestId() != 0)
						{
							leaderClan.changeAllyCrest(0, false);
						}
					}
					else
					{
						Crest crest = CrestTable.getInstance().createCrest(this._data, CrestType.ALLY);
						if (crest != null)
						{
							leaderClan.changeAllyCrest(crest.getId(), false);
							player.sendPacket(SystemMessageId.THE_CREST_WAS_SUCCESSFULLY_REGISTERED);
						}
					}
				}
				else
				{
					player.sendPacket(SystemMessageId.ACCESS_ONLY_FOR_THE_CHANNEL_FOUNDER);
				}
			}
		}
	}
}
