package net.sf.l2jdev.gameserver.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.data.xml.NpcData;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.instance.Decoy;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.script.Quest;
import net.sf.l2jdev.gameserver.model.skill.BuffInfo;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.util.Broadcast;

public class RankingPowerManager
{
	public static final int COOLDOWN = 43200000;
	public static final int LEADER_STATUE = 18485;
	private static final SkillHolder LEADER_POWER = new SkillHolder(52018, 1);
	private Decoy _decoyInstance;
	private ScheduledFuture<?> _decoyTask;

	protected RankingPowerManager()
	{
		this.reset();
	}

	public void activatePower(Player player)
	{
		Location location = player.getLocation();
		List<Integer> array = new ArrayList<>(3);
		array.add(location.getX());
		array.add(location.getY());
		array.add(location.getZ());
		GlobalVariablesManager.getInstance().setIntegerList("RANKING_POWER_LOCATION", array);
		GlobalVariablesManager.getInstance().set("RANKING_POWER_COOLDOWN", System.currentTimeMillis() + 43200000L);
		this.createClone(player);
		this.cloneTask();
		SystemMessage msg = new SystemMessage(SystemMessageId.THE_RANKING_LEADER_C1_HAS_USED_LEADER_S_POWER_IN_S2);
		msg.addString(player.getName());
		msg.addZoneName(location.getX(), location.getY(), location.getZ());
		Broadcast.toAllOnlinePlayers(msg);
	}

	private void createClone(Player player)
	{
		Location location = player.getLocation();
		NpcTemplate template = NpcData.getInstance().getTemplate(18485);
		this._decoyInstance = new Decoy(template, player, 43200000, false);
		this._decoyInstance.setTargetable(false);
		this._decoyInstance.setImmobilized(true);
		this._decoyInstance.setInvul(true);
		this._decoyInstance.spawnMe(location.getX(), location.getY(), location.getZ());
		this._decoyInstance.setHeading(location.getHeading());
		this._decoyInstance.broadcastStatusUpdate();
		Quest.addSpawn(null, 18485, location, false, 43200000);
	}

	private void cloneTask()
	{
		this._decoyTask = ThreadPool.scheduleAtFixedRate(() -> {
			World.getInstance().forEachVisibleObjectInRange(this._decoyInstance, Player.class, 300, nearby -> {
				BuffInfo info = nearby.getEffectList().getBuffInfoBySkillId(LEADER_POWER.getSkillId());
				if (info == null || info.getTime() < LEADER_POWER.getSkill().getAbnormalTime() - 60)
				{
					nearby.sendPacket(new MagicSkillUse(this._decoyInstance, nearby, LEADER_POWER.getSkillId(), LEADER_POWER.getSkillLevel(), 0, 0));
					LEADER_POWER.getSkill().applyEffects(this._decoyInstance, nearby);
				}
			});
			if (Rnd.nextBoolean())
			{
				ThreadPool.schedule(() -> this._decoyInstance.broadcastSocialAction(2), 4500L);
			}
		}, 1000L, 10000L);
		ThreadPool.schedule(this::reset, 43200000L);
	}

	public void reset()
	{
		if (this._decoyTask != null)
		{
			this._decoyTask.cancel(false);
			this._decoyTask = null;
		}

		if (this._decoyInstance != null)
		{
			this._decoyInstance.deleteMe();
		}

		GlobalVariablesManager.getInstance().remove("RANKING_POWER_COOLDOWN");
		GlobalVariablesManager.getInstance().remove("RANKING_POWER_LOCATION");
	}

	public static RankingPowerManager getInstance()
	{
		return RankingPowerManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final RankingPowerManager INSTANCE = new RankingPowerManager();
	}
}
