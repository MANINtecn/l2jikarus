package net.sf.l2jdev.gameserver.network.serverpackets.luckygame;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.enums.LuckyGameItemType;
import net.sf.l2jdev.gameserver.network.enums.LuckyGameResultType;
import net.sf.l2jdev.gameserver.network.enums.LuckyGameType;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExBettingLuckyGameResult extends ServerPacket
{
	public static final ExBettingLuckyGameResult NORMAL_INVALID_ITEM_COUNT = new ExBettingLuckyGameResult(LuckyGameResultType.INVALID_ITEM_COUNT, LuckyGameType.NORMAL);
	public static final ExBettingLuckyGameResult LUXURY_INVALID_ITEM_COUNT = new ExBettingLuckyGameResult(LuckyGameResultType.INVALID_ITEM_COUNT, LuckyGameType.LUXURY);
	public static final ExBettingLuckyGameResult NORMAL_INVALID_CAPACITY = new ExBettingLuckyGameResult(LuckyGameResultType.INVALID_CAPACITY, LuckyGameType.NORMAL);
	public static final ExBettingLuckyGameResult LUXURY_INVALID_CAPACITY = new ExBettingLuckyGameResult(LuckyGameResultType.INVALID_CAPACITY, LuckyGameType.LUXURY);
	private final LuckyGameResultType _result;
	private final LuckyGameType _type;
	private final Map<LuckyGameItemType, List<ItemHolder>> _rewards;
	private final int _ticketCount;
	private final int _size;

	public ExBettingLuckyGameResult(LuckyGameResultType result, LuckyGameType type)
	{
		this._result = result;
		this._type = type;
		this._rewards = new EnumMap<>(LuckyGameItemType.class);
		this._ticketCount = 0;
		this._size = 0;
	}

	public ExBettingLuckyGameResult(LuckyGameResultType result, LuckyGameType type, Map<LuckyGameItemType, List<ItemHolder>> rewards, int ticketCount)
	{
		this._result = result;
		this._type = type;
		this._rewards = rewards;
		this._ticketCount = ticketCount;
		this._size = (int) rewards.values().stream().mapToLong(i -> i.stream().count()).sum();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BETTING_LUCKY_GAME_RESULT.writeId(this, buffer);
		buffer.writeInt(this._result.getClientId());
		buffer.writeInt(this._type.ordinal());
		buffer.writeInt(this._ticketCount);
		buffer.writeInt(this._size);

		for (Entry<LuckyGameItemType, List<ItemHolder>> reward : this._rewards.entrySet())
		{
			for (ItemHolder item : reward.getValue())
			{
				buffer.writeInt(reward.getKey().getClientId());
				buffer.writeInt(item.getId());
				buffer.writeInt((int) item.getCount());
			}
		}
	}
}
