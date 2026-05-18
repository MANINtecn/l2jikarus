package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.CastleManorManager;
import net.sf.l2jdev.gameserver.model.Seed;
import net.sf.l2jdev.gameserver.model.SeedProduction;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExShowSeedSetting extends ServerPacket
{
	private final int _manorId;
	private final Set<Seed> _seeds;
	private final Map<Integer, SeedProduction> _current = new HashMap<>();
	private final Map<Integer, SeedProduction> _next = new HashMap<>();

	public ExShowSeedSetting(int manorId)
	{
		CastleManorManager manor = CastleManorManager.getInstance();
		this._manorId = manorId;
		this._seeds = manor.getSeedsForCastle(this._manorId);

		for (Seed s : this._seeds)
		{
			SeedProduction sp = manor.getSeedProduct(manorId, s.getSeedId(), false);
			if (sp != null)
			{
				this._current.put(s.getSeedId(), sp);
			}

			sp = manor.getSeedProduct(manorId, s.getSeedId(), true);
			if (sp != null)
			{
				this._next.put(s.getSeedId(), sp);
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_SEED_SETTING.writeId(this, buffer);
		buffer.writeInt(this._manorId);
		buffer.writeInt(this._seeds.size());

		for (Seed s : this._seeds)
		{
			buffer.writeInt(s.getSeedId());
			buffer.writeInt(s.getLevel());
			buffer.writeByte(1);
			buffer.writeInt(s.getReward(1));
			buffer.writeByte(1);
			buffer.writeInt(s.getReward(2));
			buffer.writeInt(s.getSeedLimit());
			buffer.writeInt(s.getSeedReferencePrice());
			buffer.writeInt(s.getSeedMinPrice());
			buffer.writeInt(s.getSeedMaxPrice());
			if (this._current.containsKey(s.getSeedId()))
			{
				SeedProduction sp = this._current.get(s.getSeedId());
				buffer.writeLong(sp.getStartAmount());
				buffer.writeLong(sp.getPrice());
			}
			else
			{
				buffer.writeLong(0L);
				buffer.writeLong(0L);
			}

			if (this._next.containsKey(s.getSeedId()))
			{
				SeedProduction sp = this._next.get(s.getSeedId());
				buffer.writeLong(sp.getStartAmount());
				buffer.writeLong(sp.getPrice());
			}
			else
			{
				buffer.writeLong(0L);
				buffer.writeLong(0L);
			}
		}

		this._current.clear();
		this._next.clear();
	}
}
