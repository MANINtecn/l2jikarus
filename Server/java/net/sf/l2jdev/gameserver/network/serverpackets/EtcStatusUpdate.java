package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.skill.enums.SoulType;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class EtcStatusUpdate extends ServerPacket
{
	private final Player _player;
	private int _mask;

	public EtcStatusUpdate(Player player)
	{
		this._player = player;
		this._mask = !this._player.getMessageRefusal() && !this._player.isChatBanned() && !this._player.isSilenceMode() ? 0 : 1;
		this._mask = this._mask | (this._player.isInsideZone(ZoneId.DANGER_AREA) ? 2 : 0);
		this._mask = this._mask | (this._player.hasCharmOfCourage() ? 4 : 0);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.ETC_STATUS_UPDATE.writeId(this, buffer);
		buffer.writeByte(this._player.getCharges());
		buffer.writeInt(this._player.getWeightPenalty());
		buffer.writeByte(0);
		buffer.writeByte(0);
		buffer.writeByte(0);
		buffer.writeByte(0);
		buffer.writeByte(this._mask);
		buffer.writeByte(this._player.getChargedSouls(SoulType.SHADOW));
		buffer.writeByte(this._player.getChargedSouls(SoulType.LIGHT));
	}
}
