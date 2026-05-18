package net.sf.l2jdev.gameserver.network.serverpackets.adenlab;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExAdenLabBossInfo extends ServerPacket
{
	private final int _bossId;
	private final int _currentUnlockedSlot;
	private final int _transcendEnchant;
	private final int _normalGameSaleDailyCount;
	private final int _normalGameDailyCount;
	private final Map<Byte, Map<Byte, Integer>> _specialSlots = new HashMap<>();

	@SuppressWarnings("unchecked")
	public ExAdenLabBossInfo(byte bossId, Player player)
	{
		this._bossId = bossId;
		this._currentUnlockedSlot = player.getAdenLabCurrentlyUnlockedPage(bossId);
		this._transcendEnchant = player.getAdenLabCurrentTranscendLevel(bossId);
		this._normalGameSaleDailyCount = 0;
		this._normalGameDailyCount = 0;
		this._specialSlots.putAll((Map<? extends Byte, ? extends Map<Byte, Integer>>) (player.getAdenLabSpecialGameStagesConfirmedOptions().isEmpty() ? new HashMap<>() : player.getAdenLabSpecialGameStagesConfirmedOptions().get(bossId)));
	}

	@Override
	protected void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ADENLAB_BOSS_INFO.writeId(this, buffer);
		buffer.writeInt(this._bossId);
		buffer.writeInt(this._currentUnlockedSlot);
		buffer.writeInt(this._transcendEnchant);
		buffer.writeInt(this._normalGameSaleDailyCount);
		buffer.writeInt(this._normalGameDailyCount);
		buffer.writeInt(this._specialSlots.size());

		for (Entry<Byte, Map<Byte, Integer>> slot : this._specialSlots.entrySet())
		{
			Map<Byte, Integer> values = slot.getValue();
			buffer.writeInt(slot.getKey());
			buffer.writeInt(values.size());

			for (Integer value : values.values())
			{
				buffer.writeInt(value);
			}
		}
	}
}
