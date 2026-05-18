package net.sf.l2jdev.gameserver.network.enums;

import java.util.function.Function;

import net.sf.l2jdev.gameserver.model.actor.Creature;

public enum StatusUpdateType
{
	LEVEL(1, Creature::getLevel),
	EXP(2, creature -> (int) creature.getStat().getExp()),
	STR(3, Creature::getSTR),
	DEX(4, Creature::getDEX),
	CON(5, Creature::getCON),
	INT(6, Creature::getINT),
	WIT(7, Creature::getWIT),
	MEN(8, Creature::getMEN),
	CUR_HP(9, creature -> (long) creature.getCurrentHp()),
	MAX_HP(10, creature -> creature.getMaxHp()),
	CUR_MP(11, creature -> (int) creature.getCurrentMp()),
	MAX_MP(12, Creature::getMaxMp),
	CUR_LOAD(14, Creature::getCurrentLoad),
	P_ATK(16, Creature::getPAtk),
	ATK_SPD(17, Creature::getPAtkSpd),
	P_DEF(18, Creature::getPDef),
	EVASION(19, Creature::getEvasionRate),
	ACCURACY(20, Creature::getAccuracy),
	CRITICAL(21, creature -> (int) creature.getCriticalDmg(1)),
	M_ATK(22, Creature::getMAtk),
	CAST_SPD(23, Creature::getMAtkSpd),
	M_DEF(24, Creature::getMDef),
	PVP_FLAG(25, creature -> Integer.valueOf(creature.getPvpFlag())),
	REPUTATION(26, creature -> creature.isPlayer() ? creature.asPlayer().getReputation() : 0),
	CUR_CP(32, creature -> (int) creature.getCurrentCp()),
	MAX_CP(33, Creature::getMaxCp),
	CUR_DP(39, creature -> creature.isPlayer() ? creature.asPlayer().getDeathPoints() : 0),
	MAX_DP(40, creature -> creature.isPlayer() ? creature.asPlayer().getMaxDeathPoints() : 0),
	CUR_BP(42, creature -> creature.isPlayer() ? creature.asPlayer().getBeastPoints() : 0),
	MAX_BP(43, creature -> creature.isPlayer() ? creature.asPlayer().getMaxBeastPoints() : 0),
	CUR_AP(44, creature -> creature.isPlayer() ? creature.asPlayer().getAssassinationPoints() : 0),
	MAX_AP(45, creature -> creature.isPlayer() ? creature.asPlayer().getMaxAssassinationPoints() : 0),
	CUR_LP(46, creature -> creature.isPlayer() ? creature.asPlayer().getLightPoints() : 0),
	MAX_LP(47, creature -> creature.isPlayer() ? creature.asPlayer().getMaxLightPoints() : 0),
	CUR_WP(50, creature -> creature.isPlayer() ? creature.asPlayer().getWolfPoints() : 0),
	MAX_WP(51, creature -> creature.isPlayer() ? creature.asPlayer().getMaxWolfPoints() : 0);

	private final int _clientId;
	private final Function<Creature, Number> _valueSupplier;

	private StatusUpdateType(int clientId, Function<Creature, Number> valueSupplier)
	{
		this._clientId = clientId;
		this._valueSupplier = valueSupplier;
	}

	public int getClientId()
	{
		return this._clientId;
	}

	public long getValue(Creature creature)
	{
		return this._valueSupplier.apply(creature).longValue();
	}
}
