package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.managers.CastleManorManager;
import net.sf.l2jdev.gameserver.model.CropProcure;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExShowProcureCropDetail extends ServerPacket
{
	private final int _cropId;
	private final Map<Integer, CropProcure> _castleCrops = new HashMap<>();

	public ExShowProcureCropDetail(int cropId)
	{
		this._cropId = cropId;

		for (Castle c : CastleManager.getInstance().getCastles())
		{
			CropProcure cropItem = CastleManorManager.getInstance().getCropProcure(c.getResidenceId(), cropId, false);
			if (cropItem != null && cropItem.getAmount() > 0L)
			{
				this._castleCrops.put(c.getResidenceId(), cropItem);
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_PROCURE_CROP_DETAIL.writeId(this, buffer);
		buffer.writeInt(this._cropId);
		buffer.writeInt(this._castleCrops.size());

		for (Entry<Integer, CropProcure> entry : this._castleCrops.entrySet())
		{
			CropProcure crop = entry.getValue();
			buffer.writeInt(entry.getKey());
			buffer.writeLong(crop.getAmount());
			buffer.writeLong(crop.getPrice());
			buffer.writeByte(crop.getReward());
		}
	}
}
