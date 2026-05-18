package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.config.RatesConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExVitalityEffectInfo extends ServerPacket
{
	private final int _vitalityBonus;
	private final int _vitalityItemsRemaining;
	private final int _points;

	public ExVitalityEffectInfo(Player player)
	{
		this._points = player.getVitalityPoints();
		this._vitalityBonus = (int) player.getStat().getVitalityExpBonus() * 100;
		this._vitalityItemsRemaining = RatesConfig.VITALITY_MAX_ITEMS_ALLOWED - player.getVitalityItemsUsed();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_VITALITY_EFFECT_INFO.writeId(this, buffer);
		buffer.writeInt(this._points);
		buffer.writeInt(this._vitalityBonus);
		buffer.writeShort(0);
		buffer.writeShort(this._vitalityItemsRemaining);
		buffer.writeShort(RatesConfig.VITALITY_MAX_ITEMS_ALLOWED);
	}
}
