package net.sf.l2jdev.gameserver.model.olympiad;

import net.sf.l2jdev.gameserver.config.OlympiadConfig;
import net.sf.l2jdev.gameserver.data.xml.ClassListData;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;

public class Participant
{
	private final int _objectId;
	private Player _player;
	private final String _name;
	private final String _className;
	private final int _side;
	private final int _baseClass;
	private boolean _disconnected = false;
	private boolean _defaulted = false;
	private final StatSet _stats;
	private final int _level;
	private final String _clanName;
	private final int _clanId;

	public Participant(Player plr, int olympiadSide)
	{
		this._objectId = plr.getObjectId();
		this._player = plr;
		this._name = plr.getName();
		this._className = ClassListData.getInstance().getClass(plr.getPlayerClass()).getClassName();
		this._side = olympiadSide;
		this._baseClass = plr.getBaseClass();
		this._stats = Olympiad.getNobleStats(this._objectId);
		this._clanName = plr.getClan() != null ? plr.getClan().getName() : "";
		this._clanId = plr.getClanId();
		this._level = plr.getLevel();
	}

	public Participant(int objId, int olympiadSide)
	{
		this._objectId = objId;
		this._player = null;
		this._name = "-";
		this._className = "-";
		this._side = olympiadSide;
		this._baseClass = 0;
		this._stats = null;
		this._clanName = "";
		this._clanId = 0;
		this._level = 0;
	}

	public int getLevel()
	{
		return this._level;
	}

	public boolean updatePlayer()
	{
		if (this._player == null || !this._player.isOnline())
		{
			this._player = World.getInstance().getPlayer(this.getObjectId());
		}

		return this._player != null;
	}

	public void updateStat(String statName, int increment)
	{
		this._stats.set(statName, Math.max(this._stats.getInt(statName) + increment, 0));
	}

	public String getName()
	{
		return OlympiadConfig.OLYMPIAD_HIDE_NAMES ? this._className : this._name;
	}

	public String getClanName()
	{
		return this._clanName;
	}

	public int getClanId()
	{
		return this._clanId;
	}

	public Player getPlayer()
	{
		return this._player;
	}

	public int getObjectId()
	{
		return this._objectId;
	}

	public StatSet getStats()
	{
		return this._stats;
	}

	public void setPlayer(Player noble)
	{
		this._player = noble;
	}

	public int getSide()
	{
		return this._side;
	}

	public int getBaseClass()
	{
		return this._baseClass;
	}

	public boolean isDisconnected()
	{
		return this._disconnected;
	}

	public void setDisconnected(boolean value)
	{
		this._disconnected = value;
	}

	public boolean isDefaulted()
	{
		return this._defaulted;
	}

	public void setDefaulted(boolean value)
	{
		this._defaulted = value;
	}
}
