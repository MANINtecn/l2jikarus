package net.sf.l2jdev.gameserver.network.clientpackets.friend;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.friend.ExFriendDetailInfo;

public class RequestBlockMemo extends ClientPacket
{
	private String _name;
	private String _memo;

	@Override
	protected void readImpl()
	{
		this._name = this.readString();
		this._memo = this.readString();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.updateFriendMemo(this._name, this._memo);
			player.sendPacket(new ExFriendDetailInfo(player, this._name));
		}
	}
}
