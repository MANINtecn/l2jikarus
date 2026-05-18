package net.sf.l2jdev.gameserver.model.item.holders;

public class ElementalSpiritDataHolder
{
	private int _charId;
	private int _level = 1;
	private byte _type;
	private byte _stage = 1;
	private long _experience;
	private byte _attackPoints;
	private byte _defensePoints;
	private byte _critRatePoints;
	private byte _critDamagePoints;
	private boolean _inUse;

	public ElementalSpiritDataHolder()
	{
	}

	public ElementalSpiritDataHolder(byte type, int objectId)
	{
		this._charId = objectId;
		this._type = type;
	}

	public int getCharId()
	{
		return this._charId;
	}

	public void setCharId(int charId)
	{
		this._charId = charId;
	}

	public int getLevel()
	{
		return this._level;
	}

	public void setLevel(int level)
	{
		this._level = level;
	}

	public byte getType()
	{
		return this._type;
	}

	public void setType(byte type)
	{
		this._type = type;
	}

	public byte getStage()
	{
		return this._stage;
	}

	public void setStage(byte stage)
	{
		this._stage = stage;
	}

	public long getExperience()
	{
		return this._experience;
	}

	public void setExperience(long experience)
	{
		this._experience = experience;
	}

	public byte getAttackPoints()
	{
		return this._attackPoints;
	}

	public void setAttackPoints(byte attackPoints)
	{
		this._attackPoints = attackPoints;
	}

	public byte getDefensePoints()
	{
		return this._defensePoints;
	}

	public void setDefensePoints(byte defensePoints)
	{
		this._defensePoints = defensePoints;
	}

	public byte getCritRatePoints()
	{
		return this._critRatePoints;
	}

	public void setCritRatePoints(byte critRatePoints)
	{
		this._critRatePoints = critRatePoints;
	}

	public byte getCritDamagePoints()
	{
		return this._critDamagePoints;
	}

	public void setCritDamagePoints(byte critDamagePoints)
	{
		this._critDamagePoints = critDamagePoints;
	}

	public void addExperience(long experience)
	{
		this._experience += experience;
	}

	public void increaseLevel()
	{
		this._level++;
	}

	public boolean isInUse()
	{
		return this._inUse;
	}

	public void setInUse(boolean value)
	{
		this._inUse = value;
	}

	public void addAttackPoints(byte attackPoints)
	{
		this._attackPoints += attackPoints;
	}

	public void addDefensePoints(byte defensePoints)
	{
		this._defensePoints += defensePoints;
	}

	public void addCritRatePoints(byte critRatePoints)
	{
		this._critRatePoints += critRatePoints;
	}

	public void addCritDamagePoints(byte critDamagePoints)
	{
		this._critDamagePoints += critDamagePoints;
	}

	public void increaseStage()
	{
		this._stage++;
	}
}
