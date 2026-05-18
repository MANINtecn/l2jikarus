package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.CastleManorManager;
import net.sf.l2jdev.gameserver.model.Seed;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExShowManorDefaultInfo extends ServerPacket
{
	private final List<Seed> _crops = CastleManorManager.getInstance().getCrops();
	private final boolean _hideButtons;

	public ExShowManorDefaultInfo(boolean hideButtons)
	{
		this._hideButtons = hideButtons;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_MANOR_DEFAULT_INFO.writeId(this, buffer);
		buffer.writeByte(this._hideButtons);
		buffer.writeInt(this._crops.size());

		for (Seed crop : this._crops)
		{
			buffer.writeInt(crop.getCropId());
			buffer.writeInt(crop.getLevel());
			buffer.writeInt(crop.getSeedReferencePrice());
			buffer.writeInt(crop.getCropReferencePrice());
			buffer.writeByte(1);
			buffer.writeInt(crop.getReward(1));
			buffer.writeByte(1);
			buffer.writeInt(crop.getReward(2));
		}
	}
}
