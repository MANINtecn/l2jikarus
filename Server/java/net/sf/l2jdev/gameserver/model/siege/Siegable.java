package net.sf.l2jdev.gameserver.model.siege;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.sf.l2jdev.gameserver.model.SiegeClan;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;

public interface Siegable
{
	void startSiege();

	void endSiege();

	SiegeClan getAttackerClan(int var1);

	SiegeClan getAttackerClan(Clan var1);

	Collection<SiegeClan> getAttackerClans();

	List<Player> getAttackersInZone();

	boolean checkIsAttacker(Clan var1);

	SiegeClan getDefenderClan(int var1);

	SiegeClan getDefenderClan(Clan var1);

	Collection<SiegeClan> getDefenderClans();

	boolean checkIsDefender(Clan var1);

	Set<Npc> getFlag(Clan var1);

	Calendar getSiegeDate();

	boolean giveFame();

	int getFameFrequency();

	int getFameAmount();

	void updateSiege();
}
