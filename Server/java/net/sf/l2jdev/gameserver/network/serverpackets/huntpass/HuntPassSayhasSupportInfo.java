package net.sf.l2jdev.gameserver.network.serverpackets.huntpass;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.HuntPass;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class HuntPassSayhasSupportInfo extends ServerPacket
{
	private final HuntPass _huntPass;
	private final int _timeUsed;
	private final boolean _sayhaToggle;

	public HuntPassSayhasSupportInfo(Player player)
	{
		this._huntPass = player.getHuntPass();
		this._sayhaToggle = this._huntPass.toggleSayha();
		this._timeUsed = this._huntPass.getUsedSayhaTime() + (int) (this._huntPass.getToggleStartTime() > 0 ? System.currentTimeMillis() / 1000L - this._huntPass.getToggleStartTime() : 0L);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SAYHAS_SUPPORT_INFO.writeId(this, buffer);
		buffer.writeByte(this._sayhaToggle);
		buffer.writeInt(this._huntPass.getAvailableSayhaTime());
		buffer.writeInt(this._timeUsed);
	}
}
