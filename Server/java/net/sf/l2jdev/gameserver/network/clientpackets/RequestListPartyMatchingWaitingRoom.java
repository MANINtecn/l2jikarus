package net.sf.l2jdev.gameserver.network.clientpackets;

import java.util.LinkedList;
import java.util.List;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PlayerClass;
import net.sf.l2jdev.gameserver.network.serverpackets.ExListPartyMatchingWaitingRoom;

public class RequestListPartyMatchingWaitingRoom extends ClientPacket
{
	private int _page;
	private int _minLevel;
	private int _maxLevel;
	private List<PlayerClass> _classId;
	private String _query;

	@Override
	protected void readImpl()
	{
		this._page = this.readInt();
		this._minLevel = this.readInt();
		this._maxLevel = this.readInt();
		int size = this.readInt();
		if (size > 0 && size < 128)
		{
			this._classId = new LinkedList<>();

			for (int i = 0; i < size; i++)
			{
				this._classId.add(PlayerClass.getPlayerClass(this.readInt()));
			}
		}

		if (this.remaining() > 0)
		{
			this._query = this.readString();
		}
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.sendPacket(new ExListPartyMatchingWaitingRoom(this._page, this._minLevel, this._maxLevel, this._classId, this._query));
		}
	}
}
