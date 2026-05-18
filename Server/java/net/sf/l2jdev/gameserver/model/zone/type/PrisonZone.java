package net.sf.l2jdev.gameserver.model.zone.type;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.prison.PrisonManager;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.model.zone.ZoneType;
import net.sf.l2jdev.gameserver.network.serverpackets.PlaySound;
import net.sf.l2jdev.gameserver.util.Broadcast;

public class PrisonZone extends ZoneType
{
	private static List<SkillHolder> effectsList = new ArrayList<>();
	private static Location entranceLoc;

	public PrisonZone(int id)
	{
		super(id);
	}

	@Override
	public void setParameter(String name, String value)
	{
		switch (name)
		{
			case "skillIdLvl":
				effectsList = new ArrayList<>();
				if (!value.isEmpty())
				{
					for (String skill : value.split(";"))
					{
						effectsList.add(new SkillHolder(Integer.parseInt(skill.split(",")[0]), Integer.parseInt(skill.split(",")[1])));
					}
				}
				break;
			case "defaultSpawnLoc":
				entranceLoc = null;
				if (!value.isEmpty())
				{
					entranceLoc = new Location(Integer.parseInt(value.split(",")[0]), Integer.parseInt(value.split(",")[1]), Integer.parseInt(value.split(",")[2]));
				}
				break;
			default:
				super.setParameter(name, value);
		}
	}

	@Override
	protected void onEnter(Creature creature)
	{
		if (creature.isPlayer())
		{
			creature.setInsideZone(ZoneId.PRISON, true);
			creature.setInsideZone(ZoneId.NO_BOOKMARK, true);
			creature.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
			Player player = creature.asPlayer();
			if (player.isPrisoner())
			{
				player.getPrisonerInfo().startSentenceTimer(player);
				this.applyEffects(player);
				Broadcast.toAllOnlinePlayersOnScreen(player.getName() + ", Underground Labyrinth is available.");
				Broadcast.toAllOnlinePlayers(new PlaySound("systemmsg_eu.17"));
			}
			else
			{
				player.teleToLocation(PrisonManager.getReleaseLoc(1));
			}
		}
	}

	@Override
	protected void onExit(Creature creature)
	{
		if (creature.isPlayer())
		{
			creature.setInsideZone(ZoneId.PRISON, false);
			creature.setInsideZone(ZoneId.NO_BOOKMARK, false);
			creature.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
			Player player = creature.asPlayer();
			if (player.isPrisoner())
			{
				player.getPrisonerInfo().stopSentenceTimer();
				if (player.isOnline())
				{
					if (entranceLoc != null)
					{
						player.teleToLocation(entranceLoc);
					}
					else
					{
						player.teleToLocation(-77371, -46372, -11499);
					}
				}
			}

			this.stopEffects(player);
		}
	}

	protected void applyEffects(Player player)
	{
		if (!effectsList.isEmpty())
		{
			for (SkillHolder skillH : effectsList)
			{
				skillH.getSkill().activateSkill(player, player);
			}
		}
	}

	protected void stopEffects(Player player)
	{
		if (!effectsList.isEmpty())
		{
			for (SkillHolder skillH : effectsList)
			{
				player.stopSkillEffects(null, skillH.getSkillId());
			}
		}
	}
}
