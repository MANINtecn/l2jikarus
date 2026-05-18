package net.sf.l2jdev.gameserver.model.zone.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.config.OlympiadConfig;
import net.sf.l2jdev.gameserver.data.xml.ClassListData;
import net.sf.l2jdev.gameserver.managers.ZoneManager;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.Spawn;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.actor.appearance.PlayerAppearance;
import net.sf.l2jdev.gameserver.model.actor.enums.player.TeleportWhereType;
import net.sf.l2jdev.gameserver.model.actor.instance.Door;
import net.sf.l2jdev.gameserver.model.olympiad.OlympiadGameTask;
import net.sf.l2jdev.gameserver.model.zone.AbstractZoneSettings;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.model.zone.ZoneRespawn;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.olympiad.ExOlympiadMatchEnd;

public class OlympiadStadiumZone extends ZoneRespawn
{
	private final List<Door> _doors = new ArrayList<>(2);
	private final List<Spawn> _buffers = new ArrayList<>(2);
	private final List<Location> _spectatorLocations = new ArrayList<>(1);

	public OlympiadStadiumZone(int id)
	{
		super(id);
		AbstractZoneSettings settings = ZoneManager.getSettings(this.getName());
		if (settings == null)
		{
			settings = new OlympiadStadiumZone.Settings();
		}

		this.setSettings(settings);
	}

	@Override
	public OlympiadStadiumZone.Settings getSettings()
	{
		return (OlympiadStadiumZone.Settings) super.getSettings();
	}

	@Override
	public void parseLoc(int x, int y, int z, String type)
	{
		if (type != null && type.equals("spectatorSpawn"))
		{
			this._spectatorLocations.add(new Location(x, y, z));
		}
		else
		{
			super.parseLoc(x, y, z, type);
		}
	}

	public void registerTask(OlympiadGameTask task)
	{
		this.getSettings().setTask(task);
	}

	@Override
	protected void onEnter(Creature creature)
	{
		creature.setInsideZone(ZoneId.PVP, true);
		if (creature.isPlayer())
		{
			if (OlympiadConfig.OLYMPIAD_HIDE_NAMES)
			{
				Player player = creature.asPlayer();
				PlayerAppearance appearance = player.getAppearance();
				appearance.setVisibleName(ClassListData.getInstance().getClass(player.getPlayerClass()).getClassName());
				appearance.setVisibleTitle("");
				appearance.setVisibleClanData(0, 0, 0, 0, 0);
			}

			if (this.getSettings().getOlympiadTask() != null && this.getSettings().getOlympiadTask().isBattleStarted())
			{
				creature.sendPacket(SystemMessageId.YOU_HAVE_ENTERED_A_COMBAT_ZONE);
				this.getSettings().getOlympiadTask().getGame().sendOlympiadInfo(creature);
			}
		}

		if (creature.isPlayable())
		{
			Player player = creature.asPlayer();
			if (player != null)
			{
				if (!player.isGM() && !player.isInOlympiadMode() && !player.inObserverMode())
				{
					ThreadPool.execute(new OlympiadStadiumZone.KickPlayer(player));
				}
				else
				{
					Summon pet = player.getPet();
					if (pet != null)
					{
						pet.unSummon(player);
					}
				}
			}
		}
	}

	@Override
	protected void onExit(Creature creature)
	{
		creature.setInsideZone(ZoneId.PVP, false);
		if (creature.isPlayer())
		{
			if (OlympiadConfig.OLYMPIAD_HIDE_NAMES)
			{
				PlayerAppearance appearance = creature.asPlayer().getAppearance();
				appearance.setVisibleName(null);
				appearance.setVisibleTitle(null);
				appearance.setVisibleClanData(-1, -1, -1, -1, -1);
			}

			if (this.getSettings().getOlympiadTask() != null && this.getSettings().getOlympiadTask().isBattleStarted())
			{
				creature.sendPacket(SystemMessageId.YOU_HAVE_LEFT_A_COMBAT_ZONE);
				creature.sendPacket(ExOlympiadMatchEnd.STATIC_PACKET);
			}
		}
	}

	public List<Door> getDoors()
	{
		return this._doors;
	}

	public List<Spawn> getBuffers()
	{
		return this._buffers;
	}

	public List<Location> getSpectatorSpawns()
	{
		return this._spectatorLocations;
	}

	private static class KickPlayer implements Runnable
	{
		private Player _player;

		protected KickPlayer(Player player)
		{
			this._player = player;
		}

		@Override
		public void run()
		{
			if (this._player != null)
			{
				this._player.getServitors().values().forEach(s -> s.unSummon(this._player));
				this._player.teleToLocation(TeleportWhereType.TOWN, null);
				this._player = null;
			}
		}
	}

	public class Settings extends AbstractZoneSettings
	{
		private OlympiadGameTask _task;

		protected Settings()
		{
			Objects.requireNonNull(OlympiadStadiumZone.this);
			super();
			this._task = null;
		}

		public OlympiadGameTask getOlympiadTask()
		{
			return this._task;
		}

		protected void setTask(OlympiadGameTask task)
		{
			this._task = task;
		}

		@Override
		public void clear()
		{
			this._task = null;
		}
	}
}
