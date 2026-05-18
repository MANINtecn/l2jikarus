package net.sf.l2jdev.gameserver.network.serverpackets.adenlab;

import java.util.List;
import java.util.Map;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExAdenLabSpecialProb extends ServerPacket
{
	private final int _bossId;
	private final int _pageIndex;
	private final Map<Integer, List<Integer>> _pkAdenLabSpecialGradeProb;

	public ExAdenLabSpecialProb(int bossID, int slotID, Map<Integer, List<Integer>> pkAdenLabSpecialGradeProb)
	{
		this._bossId = bossID;
		this._pageIndex = slotID;
		this._pkAdenLabSpecialGradeProb = pkAdenLabSpecialGradeProb;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ADENLAB_SPECIAL_PROB.writeId(this, buffer);
		buffer.writeInt(this._bossId);
		buffer.writeInt(this._pageIndex);
		buffer.writeInt(this._pkAdenLabSpecialGradeProb.size());

		for (List<Integer> specialGradeProb : this._pkAdenLabSpecialGradeProb.values())
		{
			buffer.writeInt(specialGradeProb.size());

			for (int prob : specialGradeProb)
			{
				buffer.writeInt(prob);
			}
		}
	}
}
