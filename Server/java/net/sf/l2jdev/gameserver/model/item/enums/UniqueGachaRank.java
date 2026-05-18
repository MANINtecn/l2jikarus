package net.sf.l2jdev.gameserver.model.item.enums;

import java.util.HashMap;
import java.util.Map;

public enum UniqueGachaRank
{
	RANK_UR(1),
	RANK_SR(2),
	RANK_R(3);

	private final int _clientId;
	private static final Map<Integer, UniqueGachaRank> VALUES = new HashMap<>(3);

	private UniqueGachaRank(int clientId)
	{
		this._clientId = clientId;
	}

	public static UniqueGachaRank getRankByClientId(int clientId)
	{
		return VALUES.getOrDefault(clientId, null);
	}

	public int getClientId()
	{
		return this._clientId;
	}

	static
	{
		for (UniqueGachaRank rank : values())
		{
			VALUES.put(rank.getClientId(), rank);
		}
	}
}
