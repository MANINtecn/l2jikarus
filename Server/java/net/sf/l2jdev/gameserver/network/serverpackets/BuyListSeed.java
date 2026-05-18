package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.CastleManorManager;
import net.sf.l2jdev.gameserver.model.SeedProduction;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class BuyListSeed extends ServerPacket
{
	private final int _manorId;
	private final long _money;
	private final List<SeedProduction> _list = new ArrayList<>();

	public BuyListSeed(long currentMoney, int castleId)
	{
		this._money = currentMoney;
		this._manorId = castleId;

		for (SeedProduction s : CastleManorManager.getInstance().getSeedProduction(castleId, false))
		{
			if (s.getAmount() > 0L && s.getPrice() > 0L)
			{
				this._list.add(s);
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.BUY_LIST_SEED.writeId(this, buffer);
		buffer.writeLong(this._money);
		buffer.writeInt(0);
		buffer.writeInt(this._manorId);
		if (!this._list.isEmpty())
		{
			buffer.writeShort(this._list.size());

			for (SeedProduction s : this._list)
			{
				buffer.writeByte(0);
				buffer.writeInt(s.getId());
				buffer.writeInt(s.getId());
				buffer.writeByte(255);
				buffer.writeLong(s.getAmount());
				buffer.writeByte(5);
				buffer.writeByte(0);
				buffer.writeShort(0);
				buffer.writeLong(0L);
				buffer.writeShort(0);
				buffer.writeInt(-1);
				buffer.writeInt(-9999);
				buffer.writeByte(1);
				buffer.writeLong(s.getPrice());
			}

			this._list.clear();
		}
		else
		{
			buffer.writeShort(0);
		}
	}
}
