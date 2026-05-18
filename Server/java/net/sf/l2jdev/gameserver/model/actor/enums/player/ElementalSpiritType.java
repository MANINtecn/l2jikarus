package net.sf.l2jdev.gameserver.model.actor.enums.player;

import net.sf.l2jdev.gameserver.model.stats.Stat;

public enum ElementalSpiritType
{
	NONE,
	FIRE,
	WATER,
	WIND,
	EARTH;

	public byte getId()
	{
		return (byte) this.ordinal();
	}

	public static ElementalSpiritType of(byte elementId)
	{
		return values()[elementId];
	}

	public boolean isSuperior(ElementalSpiritType targetType)
	{
		return this == superior(targetType);
	}

	public boolean isInferior(ElementalSpiritType targetType)
	{
		return targetType == superior(this);
	}

	public ElementalSpiritType getSuperior()
	{
		return superior(this);
	}

	public static ElementalSpiritType superior(ElementalSpiritType elementalType)
	{
		switch (elementalType)
		{
			case FIRE:
				return WATER;
			case WATER:
				return WIND;
			case WIND:
				return EARTH;
			case EARTH:
				return FIRE;
			default:
				return NONE;
		}
	}

	public Stat getAttackStat()
	{
		switch (this)
		{
			case FIRE:
				return Stat.ELEMENTAL_SPIRIT_FIRE_ATTACK;
			case WATER:
				return Stat.ELEMENTAL_SPIRIT_WATER_ATTACK;
			case WIND:
				return Stat.ELEMENTAL_SPIRIT_WIND_ATTACK;
			case EARTH:
				return Stat.ELEMENTAL_SPIRIT_EARTH_ATTACK;
			default:
				return null;
		}
	}

	public Stat getDefenseStat()
	{
		switch (this)
		{
			case FIRE:
				return Stat.ELEMENTAL_SPIRIT_FIRE_DEFENSE;
			case WATER:
				return Stat.ELEMENTAL_SPIRIT_WATER_DEFENSE;
			case WIND:
				return Stat.ELEMENTAL_SPIRIT_WIND_DEFENSE;
			case EARTH:
				return Stat.ELEMENTAL_SPIRIT_EARTH_DEFENSE;
			default:
				return null;
		}
	}

	public String getName()
	{
		switch (this)
		{
			case FIRE:
				return "Fire";
			case WATER:
				return "Water";
			case WIND:
				return "Wind";
			case EARTH:
				return "Earth";
			default:
				return "None";
		}
	}
}
