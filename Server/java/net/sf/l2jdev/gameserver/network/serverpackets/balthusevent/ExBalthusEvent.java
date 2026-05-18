package net.sf.l2jdev.gameserver.network.serverpackets.balthusevent;

import java.util.Calendar;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.events.BalthusEventManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExBalthusEvent extends ServerPacket
{
	private final int _rewardItemId;
	private final int _currentProgress;
	private final int _rewardTokenCount;
	private final int _consolationCount;
	private final boolean _isParticipant;
	private final boolean _isRunning;
	private final int _hour;

	public ExBalthusEvent(Player player)
	{
		BalthusEventManager manager = BalthusEventManager.getInstance();
		ItemHolder currentReward = manager.getCurrRewardItem();
		this._rewardItemId = currentReward != null ? currentReward.getId() : 57;
		this._currentProgress = manager.getCurrentProgress() * 20;
		this._rewardTokenCount = player.getVariables().getInt("BALTHUS_REWARD", 0);
		this._consolationCount = (int) Math.min(manager.getConsolation().getCount(), 2147483647L);
		this._isParticipant = manager.isPlayerParticipant(player);
		this._isRunning = manager.isRunning();
		this._hour = Calendar.getInstance().get(10);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BALTHUS_EVENT.writeId(this, buffer);
		buffer.writeInt(this._hour);
		buffer.writeInt(this._currentProgress);
		buffer.writeInt(this._rewardItemId);
		buffer.writeInt(this._rewardTokenCount);
		buffer.writeInt(this._consolationCount);
		buffer.writeInt(this._isParticipant);
		buffer.writeByte(this._isRunning);
		buffer.writeInt(this._hour * 60);
	}
}
