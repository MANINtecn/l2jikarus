package net.sf.l2jdev.gameserver.model.actor.holders.player;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.xml.ExperienceData;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PlayerClass;

public class SubClassHolder
{
	private static final int MAX_LEVEL = PlayerConfig.MAX_SUBCLASS_LEVEL < ExperienceData.getInstance().getMaxLevel() ? PlayerConfig.MAX_SUBCLASS_LEVEL : ExperienceData.getInstance().getMaxLevel() - 1;
	public static final int MAX_VITALITY_POINTS = 3500000;
	public static final int MIN_VITALITY_POINTS = 0;
	private PlayerClass _playerClass;
	private long _exp = ExperienceData.getInstance().getExpForLevel(PlayerConfig.BASE_SUBCLASS_LEVEL);
	private long _sp = 0L;
	private int _level = PlayerConfig.BASE_SUBCLASS_LEVEL;
	private int _classIndex = 1;
	private int _vitalityPoints = 0;
	private boolean _dualClass = false;

	public PlayerClass getPlayerClass()
	{
		return this._playerClass;
	}

	public int getId()
	{
		return this._playerClass.getId();
	}

	public long getExp()
	{
		return this._exp;
	}

	public long getSp()
	{
		return this._sp;
	}

	public int getLevel()
	{
		return this._level;
	}

	public int getVitalityPoints()
	{
		return Math.min(Math.max(this._vitalityPoints, 0), 3500000);
	}

	public void setVitalityPoints(int value)
	{
		this._vitalityPoints = Math.min(Math.max(value, 0), 3500000);
	}

	public int getClassIndex()
	{
		return this._classIndex;
	}

	public void setPlayerClass(int id)
	{
		this._playerClass = PlayerClass.getPlayerClass(id);
	}

	public void setExp(long expValue)
	{
		if (!this._dualClass && expValue > ExperienceData.getInstance().getExpForLevel(MAX_LEVEL + 1) - 1L)
		{
			this._exp = ExperienceData.getInstance().getExpForLevel(MAX_LEVEL + 1) - 1L;
		}
		else
		{
			this._exp = expValue;
		}
	}

	public void setSp(long spValue)
	{
		this._sp = spValue;
	}

	public void setClassIndex(int classIndex)
	{
		this._classIndex = classIndex;
	}

	public boolean isDualClass()
	{
		return this._dualClass;
	}

	public void setDualClassActive(boolean dualClass)
	{
		this._dualClass = dualClass;
	}

	public void setLevel(int levelValue)
	{
		if (!this._dualClass && levelValue > MAX_LEVEL)
		{
			this._level = MAX_LEVEL;
		}
		else if (levelValue < PlayerConfig.BASE_SUBCLASS_LEVEL)
		{
			this._level = PlayerConfig.BASE_SUBCLASS_LEVEL;
		}
		else
		{
			this._level = levelValue;
		}
	}
}
