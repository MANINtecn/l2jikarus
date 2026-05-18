package net.sf.l2jdev.gameserver.model.clan.entry;

import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;

public class PledgeWaitingInfo
{
	private int _playerId;
	private int _playerClassId;
	private int _playerLvl;
	private final int _karma;
	private String _playerName;

	public PledgeWaitingInfo(int playerId, int playerLvl, int karma, int classId, String playerName)
	{
		this._playerId = playerId;
		this._playerClassId = classId;
		this._playerLvl = playerLvl;
		this._karma = karma;
		this._playerName = playerName;
	}

	public int getPlayerId()
	{
		return this._playerId;
	}

	public void setPlayerId(int playerId)
	{
		this._playerId = playerId;
	}

	public int getPlayerClassId()
	{
		if (this.isOnline() && this.getPlayer().getBaseClass() != this._playerClassId)
		{
			this._playerClassId = this.getPlayer().getPlayerClass().getId();
		}

		return this._playerClassId;
	}

	public int getPlayerLvl()
	{
		if (this.isOnline() && this.getPlayer().getLevel() != this._playerLvl)
		{
			this._playerLvl = this.getPlayer().getLevel();
		}

		return this._playerLvl;
	}

	public int getKarma()
	{
		return this._karma;
	}

	public String getPlayerName()
	{
		if (this.isOnline() && !this.getPlayer().getName().equalsIgnoreCase(this._playerName))
		{
			this._playerName = this.getPlayer().getName();
		}

		return this._playerName;
	}

	public Player getPlayer()
	{
		return World.getInstance().getPlayer(this._playerId);
	}

	public boolean isOnline()
	{
		return this.getPlayer() != null && this.getPlayer().isOnlineInt() > 0;
	}
}
