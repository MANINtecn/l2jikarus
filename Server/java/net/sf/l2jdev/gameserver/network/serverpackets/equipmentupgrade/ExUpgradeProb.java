package net.sf.l2jdev.gameserver.network.serverpackets.equipmentupgrade;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExUpgradeProb extends ServerPacket
{
	private final int _upgradeId;
	private final double _probability;

	public ExUpgradeProb(int upgradeId, double probability)
	{
		this._upgradeId = upgradeId;
		this._probability = probability;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_UPGRADE_PROB.writeId(this, buffer);
		buffer.writeInt(this._upgradeId);
		buffer.writeInt((int) Math.floor(this._probability * 10000.0));
	}
}
