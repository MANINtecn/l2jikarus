package net.sf.l2jdev.gameserver.network.loginserverpackets.login;

import net.sf.l2jdev.commons.network.base.BaseReadablePacket;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;

public class ChangePasswordResponse extends BaseReadablePacket
{
	public ChangePasswordResponse(byte[] decrypt)
	{
		super(decrypt);
		this.readByte();
		String character = this.readString();
		String msgToSend = this.readString();
		Player player = World.getInstance().getPlayer(character);
		if (player != null)
		{
			player.sendMessage(msgToSend);
		}
	}
}
