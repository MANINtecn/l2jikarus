package net.sf.l2jdev.gameserver.model.clan.enums;

import java.util.function.Function;

import net.sf.l2jdev.gameserver.data.xml.ClanRewardData;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.ClanRewardBonus;

public enum ClanRewardType
{
	MEMBERS_ONLINE(0, Clan::getPreviousMaxOnlinePlayers),
	HUNTING_MONSTERS(1, Clan::getPreviousHuntingPoints);

	private final int _clientId;
	private final int _mask;
	private final Function<Clan, Integer> _pointsFunction;

	private ClanRewardType(int clientId, Function<Clan, Integer> pointsFunction)
	{
		this._clientId = clientId;
		this._mask = 1 << clientId;
		this._pointsFunction = pointsFunction;
	}

	public int getClientId()
	{
		return this._clientId;
	}

	public int getMask()
	{
		return this._mask;
	}

	public ClanRewardBonus getAvailableBonus(Clan clan)
	{
		ClanRewardBonus availableBonus = null;

		for (ClanRewardBonus bonus : ClanRewardData.getInstance().getClanRewardBonuses(this))
		{
			if (bonus.getRequiredAmount() <= this._pointsFunction.apply(clan) && (availableBonus == null || availableBonus.getLevel() < bonus.getLevel()))
			{
				availableBonus = bonus;
			}
		}

		return availableBonus;
	}

	public static int getDefaultMask()
	{
		int mask = 0;

		for (ClanRewardType type : values())
		{
			mask |= type.getMask();
		}

		return mask;
	}
}
