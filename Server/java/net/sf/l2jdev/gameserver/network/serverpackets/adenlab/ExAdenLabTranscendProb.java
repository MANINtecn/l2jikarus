package net.sf.l2jdev.gameserver.network.serverpackets.adenlab;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExAdenLabTranscendProb extends ServerPacket
{
	private final int _bossId;
	private final List<Integer> _probs;

	public ExAdenLabTranscendProb(int bossId, List<Integer> probabilities)
	{
		this._bossId = bossId;
		this._probs = probabilities;
	}

	@Override
	protected void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ADENLAB_TRANSCEND_PROB.writeId(this, buffer);
		buffer.writeInt(this._bossId);
		buffer.writeInt(this._probs.size());

		for (int probability : this._probs)
		{
			buffer.writeInt(probability);
		}
	}
}
