package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.CastleManorManager;
import net.sf.l2jdev.gameserver.model.CropProcure;
import net.sf.l2jdev.gameserver.model.Seed;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExShowCropInfo extends ServerPacket
{
	private final List<CropProcure> _crops;
	private final int _manorId;
	private final boolean _hideButtons;

	public ExShowCropInfo(int manorId, boolean nextPeriod, boolean hideButtons)
	{
		this._manorId = manorId;
		this._hideButtons = hideButtons;
		CastleManorManager manor = CastleManorManager.getInstance();
		this._crops = nextPeriod && !manor.isManorApproved() ? null : manor.getCropProcure(manorId, nextPeriod);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_CROP_INFO.writeId(this, buffer);
		buffer.writeByte(this._hideButtons);
		buffer.writeInt(this._manorId);
		buffer.writeInt(0);
		if (this._crops != null)
		{
			buffer.writeInt(this._crops.size());

			for (CropProcure crop : this._crops)
			{
				buffer.writeInt(crop.getId());
				buffer.writeLong(crop.getAmount());
				buffer.writeLong(crop.getStartAmount());
				buffer.writeLong(crop.getPrice());
				buffer.writeByte(crop.getReward());
				Seed seed = CastleManorManager.getInstance().getSeedByCrop(crop.getId());
				if (seed == null)
				{
					buffer.writeInt(0);
					buffer.writeByte(1);
					buffer.writeInt(0);
					buffer.writeByte(1);
					buffer.writeInt(0);
				}
				else
				{
					buffer.writeInt(seed.getLevel());
					buffer.writeByte(1);
					buffer.writeInt(seed.getReward(1));
					buffer.writeByte(1);
					buffer.writeInt(seed.getReward(2));
				}
			}
		}
	}
}
