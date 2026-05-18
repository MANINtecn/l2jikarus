package net.sf.l2jdev.gameserver.model.stats.finalizers;

import java.util.OptionalDouble;

import net.sf.l2jdev.gameserver.config.NpcConfig;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.xml.ClanHallData;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.managers.FortManager;
import net.sf.l2jdev.gameserver.managers.ZoneManager;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.residences.AbstractResidence;
import net.sf.l2jdev.gameserver.model.residences.ResidenceFunction;
import net.sf.l2jdev.gameserver.model.residences.ResidenceFunctionType;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.model.siege.Fort;
import net.sf.l2jdev.gameserver.model.stats.BaseStat;
import net.sf.l2jdev.gameserver.model.stats.IStatFunction;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.model.zone.type.CastleZone;
import net.sf.l2jdev.gameserver.model.zone.type.ClanHallZone;
import net.sf.l2jdev.gameserver.model.zone.type.FortZone;
import net.sf.l2jdev.gameserver.model.zone.type.MotherTreeZone;

public class RegenMPFinalizer implements IStatFunction
{
	@Override
	public double calc(Creature creature, OptionalDouble base, Stat stat)
	{
		this.throwIfPresent(base);
		double baseValue = creature.isPlayer() ? creature.asPlayer().getTemplate().getBaseMpRegen(creature.getLevel()) : creature.getTemplate().getBaseMpReg();
		baseValue *= creature.isRaid() ? NpcConfig.RAID_MP_REGEN_MULTIPLIER : PlayerConfig.MP_REGEN_MULTIPLIER;
		if (creature.isPlayer())
		{
			Player player = creature.asPlayer();
			Clan clan = player.getClan();
			if (player.isInsideZone(ZoneId.CLAN_HALL) && clan != null && clan.getHideoutId() > 0)
			{
				ClanHallZone zone = ZoneManager.getInstance().getZone(player, ClanHallZone.class);
				int posChIndex = zone == null ? -1 : zone.getResidenceId();
				int clanHallIndex = clan.getHideoutId();
				if (clanHallIndex > 0 && clanHallIndex == posChIndex)
				{
					AbstractResidence residense = ClanHallData.getInstance().getClanHallById(clan.getHideoutId());
					if (residense != null)
					{
						ResidenceFunction func = residense.getFunction(ResidenceFunctionType.MP_REGEN);
						if (func != null)
						{
							baseValue *= func.getValue();
						}
					}
				}
			}

			if (player.isInsideZone(ZoneId.CASTLE) && clan != null && clan.getCastleId() > 0)
			{
				CastleZone zone = ZoneManager.getInstance().getZone(player, CastleZone.class);
				int posCastleIndex = zone == null ? -1 : zone.getResidenceId();
				int castleIndex = clan.getCastleId();
				if (castleIndex > 0 && castleIndex == posCastleIndex)
				{
					Castle castle = CastleManager.getInstance().getCastleById(clan.getCastleId());
					if (castle != null)
					{
						Castle.CastleFunction func = castle.getCastleFunction(3);
						if (func != null)
						{
							baseValue *= func.getLvl() / 100;
						}
					}
				}
			}

			if (player.isInsideZone(ZoneId.FORT) && clan != null && clan.getFortId() > 0)
			{
				FortZone zone = ZoneManager.getInstance().getZone(player, FortZone.class);
				int posFortIndex = zone == null ? -1 : zone.getResidenceId();
				int fortIndex = clan.getFortId();
				if (fortIndex > 0 && fortIndex == posFortIndex)
				{
					Fort fort = FortManager.getInstance().getFortById(clan.getCastleId());
					if (fort != null)
					{
						Fort.FortFunction func = fort.getFortFunction(3);
						if (func != null)
						{
							baseValue *= func.getLevel() / 100;
						}
					}
				}
			}

			if (player.isInsideZone(ZoneId.MOTHER_TREE))
			{
				MotherTreeZone zone = ZoneManager.getInstance().getZone(player, MotherTreeZone.class);
				int mpBonus = zone == null ? 0 : zone.getMpRegenBonus();
				baseValue += mpBonus;
			}

			if (player.isSitting())
			{
				baseValue *= 1.5;
			}
			else if (!player.isMoving())
			{
				baseValue *= 1.1;
			}
			else if (player.isRunning())
			{
				baseValue *= 0.7;
			}

			baseValue *= creature.getLevelMod() * BaseStat.MEN.calcBonus(creature);
		}
		else if (creature.isPet())
		{
			baseValue = creature.asPet().getPetLevelData().getPetRegenMP() * NpcConfig.PET_MP_REGEN_MULTIPLIER;
		}

		return Stat.defaultValue(creature, stat, baseValue);
	}
}
