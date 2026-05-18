package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.CastleManorManager;
import net.sf.l2jdev.gameserver.model.CropProcure;
import net.sf.l2jdev.gameserver.model.Seed;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExShowCropSetting extends ServerPacket
{
	private final int _manorId;
	private final Set<Seed> _seeds;
	private final Map<Integer, CropProcure> _current = new HashMap<>();
	private final Map<Integer, CropProcure> _next = new HashMap<>();

	public ExShowCropSetting(int manorId)
	{
		CastleManorManager manor = CastleManorManager.getInstance();
		this._manorId = manorId;
		this._seeds = manor.getSeedsForCastle(this._manorId);

		for (Seed s : this._seeds)
		{
			CropProcure cp = manor.getCropProcure(manorId, s.getCropId(), false);
			if (cp != null)
			{
				this._current.put(s.getCropId(), cp);
			}

			cp = manor.getCropProcure(manorId, s.getCropId(), true);
			if (cp != null)
			{
				this._next.put(s.getCropId(), cp);
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_CROP_SETTING.writeId(this, buffer);
		buffer.writeInt(this._manorId);
		buffer.writeInt(this._seeds.size());

		for (Seed s : this._seeds)
		{
			buffer.writeInt(s.getCropId());
			buffer.writeInt(s.getLevel());
			buffer.writeByte(1);
			buffer.writeInt(s.getReward(1));
			buffer.writeByte(1);
			buffer.writeInt(s.getReward(2));
			buffer.writeInt(s.getCropLimit());
			buffer.writeInt(0);
			buffer.writeInt(s.getCropMinPrice());
			buffer.writeInt(s.getCropMaxPrice());
			if (this._current.containsKey(s.getCropId()))
			{
				CropProcure cp = this._current.get(s.getCropId());
				buffer.writeLong(cp.getStartAmount());
				buffer.writeLong(cp.getPrice());
				buffer.writeByte(cp.getReward());
			}
			else
			{
				buffer.writeLong(0L);
				buffer.writeLong(0L);
				buffer.writeByte(0);
			}

			if (this._next.containsKey(s.getCropId()))
			{
				CropProcure cp = this._next.get(s.getCropId());
				buffer.writeLong(cp.getStartAmount());
				buffer.writeLong(cp.getPrice());
				buffer.writeByte(cp.getReward());
			}
			else
			{
				buffer.writeLong(0L);
				buffer.writeLong(0L);
				buffer.writeByte(0);
			}
		}

		this._next.clear();
		this._current.clear();
	}
}
