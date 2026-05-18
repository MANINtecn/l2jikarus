package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.FortSiegeManager;
import net.sf.l2jdev.gameserver.model.FortSiegeSpawn;
import net.sf.l2jdev.gameserver.model.Spawn;
import net.sf.l2jdev.gameserver.model.siege.Fort;
import net.sf.l2jdev.gameserver.model.siege.FortSiege;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExShowFortressMapInfo extends ServerPacket
{
	private final Fort _fortress;
	private final FortSiege _siege;
	private final List<FortSiegeSpawn> _commanders;

	public ExShowFortressMapInfo(Fort fortress)
	{
		this._fortress = fortress;
		this._siege = fortress.getSiege();
		this._commanders = FortSiegeManager.getInstance().getCommanderSpawnList(fortress.getResidenceId());
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_FORTRESS_MAP_INFO.writeId(this, buffer);
		buffer.writeInt(this._fortress.getResidenceId());
		buffer.writeInt(this._siege.isInProgress());
		buffer.writeInt(this._fortress.getFortSize());
		if (this._commanders != null && !this._commanders.isEmpty() && this._siege.isInProgress())
		{
			switch (this._commanders.size())
			{
				case 3:
					for (FortSiegeSpawn spawn : this._commanders)
					{
						if (this.isSpawned(spawn.getId()))
						{
							buffer.writeInt(0);
						}
						else
						{
							buffer.writeInt(1);
						}
					}
					break;
				case 4:
					int count = 0;

					for (FortSiegeSpawn spawn : this._commanders)
					{
						if (++count == 4)
						{
							buffer.writeInt(1);
						}

						if (this.isSpawned(spawn.getId()))
						{
							buffer.writeInt(0);
						}
						else
						{
							buffer.writeInt(1);
						}
					}
			}
		}
		else
		{
			for (int i = 0; i < this._fortress.getFortSize(); i++)
			{
				buffer.writeInt(0);
			}
		}
	}

	private boolean isSpawned(int npcId)
	{
		boolean ret = false;

		for (Spawn spawn : this._siege.getCommanders())
		{
			if (spawn.getId() == npcId)
			{
				ret = true;
				break;
			}
		}

		return ret;
	}
}
