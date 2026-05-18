package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.InstanceManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExInzoneWaiting extends ServerPacket
{
	private final int _currentTemplateId;
	private final Map<Integer, Long> _instanceTimes;
	private final boolean _hide;

	public ExInzoneWaiting(Player player, boolean hide)
	{
		Instance instance = InstanceManager.getInstance().getPlayerInstance(player, false);
		this._currentTemplateId = instance != null && instance.getTemplateId() >= 0 ? instance.getTemplateId() : -1;
		this._instanceTimes = InstanceManager.getInstance().getAllInstanceTimes(player);
		this._hide = hide;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_INZONE_WAITING_INFO.writeId(this, buffer);
		buffer.writeByte(!this._hide);
		buffer.writeInt(this._currentTemplateId);
		buffer.writeInt(this._instanceTimes.size());

		for (Entry<Integer, Long> entry : this._instanceTimes.entrySet())
		{
			long instanceTime = TimeUnit.MILLISECONDS.toSeconds(entry.getValue() - System.currentTimeMillis());
			buffer.writeInt(entry.getKey());
			buffer.writeInt((int) instanceTime);
		}
	}
}
