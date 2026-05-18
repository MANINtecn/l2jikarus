package net.sf.l2jdev.gameserver.network.serverpackets.gacha;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.events.UniqueGachaManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.holders.GachaItemHolder;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class UniqueGachaOpen extends ServerPacket
{
	 
	private final int _fullInfo;
	private final int _openMode;
	private final long _currencyCount;
	private final int _stepsToGuaranteedReward;
	private final int _remainingTime;
	private final boolean _isShowProbability;
	private final Set<GachaItemHolder> _visibleItems;
	private final int _totalRewardCount;
	private final Collection<Set<GachaItemHolder>> _rewardItems;
	private final int _currencyItemId;
	private final Map<Integer, Long> _gameCosts;

	public UniqueGachaOpen(Player player, int fullInfo, int openMode)
	{
		this._fullInfo = fullInfo;
		this._openMode = openMode;
		UniqueGachaManager manager = UniqueGachaManager.getInstance();
		this._currencyCount = manager.getCurrencyCount(player);
		this._stepsToGuaranteedReward = manager.getStepsToGuaranteedReward(player);
		this._remainingTime = (int) ((manager.getActiveUntilPeriod() - System.currentTimeMillis()) / 1000L);
		this._isShowProbability = manager.isShowProbability();
		if (this._fullInfo == 1)
		{
			this._visibleItems = manager.getVisibleItems();
			this._totalRewardCount = manager.getTotalRewardCount();
			this._rewardItems = manager.getRewardItems().values();
			this._currencyItemId = manager.getCurrencyItemId();
			this._gameCosts = manager.getGameCosts();
		}
		else
		{
			this._visibleItems = Collections.emptySet();
			this._totalRewardCount = 0;
			this._rewardItems = Collections.emptySet();
			this._currencyItemId = 0;
			this._gameCosts = Collections.emptyMap();
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_UNIQUE_GACHA_OPEN.writeId(this, buffer);
		buffer.writeByte(this._openMode);
		buffer.writeByte(0);
		buffer.writeLong(this._currencyCount);
		buffer.writeInt(this._stepsToGuaranteedReward);
		buffer.writeInt(this._remainingTime);
		buffer.writeByte(this._fullInfo);
		buffer.writeByte(this._isShowProbability);
		if (this._fullInfo == 1)
		{
			this.writeFullInfo(buffer);
		}
		else
		{
			this.writeDummyInfo(buffer);
		}
	}

	private void writeFullInfo(WritableBuffer buffer)
	{
		buffer.writeInt(this._visibleItems.size());

		for (GachaItemHolder item : this._visibleItems)
		{
			buffer.writeShort(15);
			buffer.writeByte(item.getRank().getClientId());
			buffer.writeInt(item.getId());
			buffer.writeLong(item.getCount());
			buffer.writeDouble(this.getChance(item.getItemChance()));
		}

		buffer.writeInt(this._totalRewardCount);

		for (Set<GachaItemHolder> items : this._rewardItems)
		{
			for (GachaItemHolder item : items)
			{
				buffer.writeShort(15);
				buffer.writeByte(item.getRank().getClientId());
				buffer.writeInt(item.getId());
				buffer.writeLong(item.getCount());
				buffer.writeDouble(this.getChance(item.getItemChance()));
			}
		}

		buffer.writeByte(1);
		buffer.writeInt(this._currencyItemId);
		buffer.writeInt(this._gameCosts.size());

		for (Entry<Integer, Long> entry : this._gameCosts.entrySet())
		{
			buffer.writeInt(entry.getKey());
			buffer.writeLong(entry.getValue());
		}
	}

	protected void writeDummyInfo(WritableBuffer buffer)
	{
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeByte(0);
		buffer.writeInt(0);
		buffer.writeInt(0);
	}

	private double getChance(int itemChance)
	{
		return this._isShowProbability ? itemChance / 1000000.0 : 0.0;
	}
}
