package net.sf.l2jdev.gameserver.network.clientpackets.raidbossinfo;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2jdev.gameserver.managers.DatabaseSpawnManager;
import net.sf.l2jdev.gameserver.managers.GrandBossManager;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.enums.npc.RaidBossStatus;
import net.sf.l2jdev.gameserver.model.actor.instance.GrandBoss;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.raidbossinfo.ExRaidBossSpawnInfo;

public class RequestRaidBossSpawnInfo extends ClientPacket
{
 
	private final Map<Integer, RaidBossStatus> _statuses = new HashMap<>();

	@Override
	protected void readImpl()
	{
		int count = this.readInt();

		for (int i = 0; i < count; i++)
		{
			int bossId = this.readInt();
			GrandBoss boss = GrandBossManager.getInstance().getBoss(bossId);
			if (boss == null)
			{
				RaidBossStatus status = DatabaseSpawnManager.getInstance().getStatus(bossId);
				if (status != RaidBossStatus.UNDEFINED)
				{
					Npc npc = DatabaseSpawnManager.getInstance().getNpc(bossId);
					if (npc != null && npc.isInCombat())
					{
						this._statuses.put(bossId, RaidBossStatus.COMBAT);
					}
					else
					{
						this._statuses.put(bossId, status);
					}
				}
				else
				{
					this._statuses.put(bossId, RaidBossStatus.DEAD);
				}
			}
			else if (!boss.isDead() && boss.isSpawned())
			{
				if (boss.isInCombat())
				{
					this._statuses.put(bossId, RaidBossStatus.COMBAT);
				}
				else
				{
					this._statuses.put(bossId, RaidBossStatus.ALIVE);
				}
			}
			else if (bossId == 29020 && GrandBossManager.getInstance().getStatus(29020) == 0)
			{
				this._statuses.put(bossId, RaidBossStatus.ALIVE);
			}
			else
			{
				this._statuses.put(bossId, RaidBossStatus.DEAD);
			}
		}
	}

	@Override
	protected void runImpl()
	{
		this.getClient().sendPacket(new ExRaidBossSpawnInfo(this._statuses));
	}
}
