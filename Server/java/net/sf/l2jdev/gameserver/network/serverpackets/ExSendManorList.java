package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExSendManorList extends ServerPacket
{
	public static final ExSendManorList STATIC_PACKET = new ExSendManorList();
	private final Collection<Castle> _castles = CastleManager.getInstance().getCastles();

	private ExSendManorList()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SEND_MANOR_LIST.writeId(this, buffer);
		buffer.writeInt(this._castles.size());

		for (Castle castle : this._castles)
		{
			buffer.writeInt(castle.getResidenceId());
		}
	}
}
