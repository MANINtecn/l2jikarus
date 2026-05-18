package net.sf.l2jdev.gameserver.network.serverpackets.gacha;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.events.UniqueGachaManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.holders.GachaItemHolder;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class UniqueGachaGame extends ServerPacket
{
	public static final int FAILURE = 0;
	public static final int SUCCESS = 1;
	private final int _success;
	private final Player _player;
	private final List<GachaItemHolder> _rewards;
	private final boolean _rare;
	private final long _currencyCount;
	private final int _guaranteedReward;

	public UniqueGachaGame(int success, Player player, List<GachaItemHolder> rewards, boolean rare)
	{
		this._success = success;
		this._player = player;
		this._rewards = rewards;
		this._rare = rare;
		UniqueGachaManager manager = UniqueGachaManager.getInstance();
		this._currencyCount = manager.getCurrencyCount(this._player);
		this._guaranteedReward = manager.getStepsToGuaranteedReward(this._player);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_UNIQUE_GACHA_GAME.writeId(this, buffer);
		buffer.writeByte(this._success);
		buffer.writeLong(this._currencyCount);
		buffer.writeInt(this._guaranteedReward);
		buffer.writeByte(this._rare ? 1 : 0);
		buffer.writeInt(this._rewards.size());

		for (GachaItemHolder item : this._rewards)
		{
			buffer.writeByte(item.getRank().getClientId());
			buffer.writeInt(item.getId());
			buffer.writeLong(item.getCount());
		}
	}
}
