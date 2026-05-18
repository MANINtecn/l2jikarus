package net.sf.l2jdev.gameserver.network.loginserverpackets.game;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sf.l2jdev.commons.network.base.BaseWritablePacket;

public class PlayerInGame extends BaseWritablePacket
{
	 
	private final List<String> _players;

	public PlayerInGame(String player)
	{
		this(Arrays.asList(player != null ? player : ""));
	}

	public PlayerInGame(List<String> players)
	{
		this._players = players != null ? players : Collections.emptyList();
	}

	@Override
	public void write()
	{
		this.writeByte(2);
		this.writeShort(this._players.size());

		for (String p : this._players)
		{
			this.writeString(p != null ? p : "");
		}
	}
}
