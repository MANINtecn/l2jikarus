package net.sf.l2jdev.gameserver.model.clan.entry;

import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;

public class PledgeApplicantInfo
{
	private final int _playerId;
	private final int _requestClanId;
	private String _playerName;
	private int _playerLvl;
	private int _classId;
	private final int _karma;
	private final String _message;

	public PledgeApplicantInfo(int playerId, String playerName, int playerLevel, int karma, int requestClanId, String message)
	{
		this._playerId = playerId;
		this._requestClanId = requestClanId;
		this._playerName = playerName;
		this._playerLvl = playerLevel;
		this._karma = karma;
		this._message = message;
	}

	public int getPlayerId()
	{
		return this._playerId;
	}

	public int getRequestClanId()
	{
		return this._requestClanId;
	}

	public String getPlayerName()
	{
		if (this.isOnline() && !this.getPlayer().getName().equalsIgnoreCase(this._playerName))
		{
			this._playerName = this.getPlayer().getName();
		}

		return this._playerName;
	}

	public int getPlayerLvl()
	{
		if (this.isOnline() && this.getPlayer().getLevel() != this._playerLvl)
		{
			this._playerLvl = this.getPlayer().getLevel();
		}

		return this._playerLvl;
	}

	public int getClassId()
	{
		if (this.isOnline() && this.getPlayer().getBaseClass() != this._classId)
		{
			this._classId = this.getPlayer().getPlayerClass().getId();
		}

		return this._classId;
	}

	public String getMessage()
	{
		return this._message;
	}

	public int getKarma()
	{
		return this._karma;
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
