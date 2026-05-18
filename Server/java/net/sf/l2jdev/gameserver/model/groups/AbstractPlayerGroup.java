package net.sf.l2jdev.gameserver.model.groups;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.model.BlockList;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Race;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public abstract class AbstractPlayerGroup
{
	public abstract List<Player> getMembers();

	public List<Integer> getMembersObjectId()
	{
		List<Integer> ids = new ArrayList<>();
		this.forEachMember(m -> {
			ids.add(m.getObjectId());
			return true;
		});
		return ids;
	}

	public abstract Player getLeader();

	public abstract void setLeader(Player var1);

	public int getLeaderObjectId()
	{
		Player leader = this.getLeader();
		return leader == null ? 0 : leader.getObjectId();
	}

	public boolean isLeader(Player player)
	{
		if (player == null)
		{
			return false;
		}
		Player leader = this.getLeader();
		return leader == null ? false : leader.getObjectId() == player.getObjectId();
	}

	public int getMemberCount()
	{
		return this.getMembers().size();
	}

	public int getRaceCount()
	{
		List<Race> partyRaces = new ArrayList<>();

		for (Player member : this.getMembers())
		{
			if (!partyRaces.contains(member.getRace()))
			{
				partyRaces.add(member.getRace());
			}
		}

		return partyRaces.size();
	}

	public abstract int getLevel();

	public void broadcastPacket(ServerPacket packet)
	{
		this.forEachMember(m -> {
			if (m != null)
			{
				m.sendPacket(packet);
			}

			return true;
		});
	}

	public void broadcastMessage(SystemMessageId message)
	{
		this.broadcastPacket(new SystemMessage(message));
	}

	public void broadcastString(String text)
	{
		this.broadcastPacket(new SystemMessage(text));
	}

	public void broadcastCreatureSay(CreatureSay msg, Player broadcaster)
	{
		this.forEachMember(m -> {
			if (m != null && !BlockList.isBlocked(m, broadcaster))
			{
				m.sendPacket(msg);
			}

			return true;
		});
	}

	public boolean containsPlayer(Player player)
	{
		return this.getMembers().contains(player);
	}

	public Player getRandomPlayer()
	{
		return this.getMembers().get(Rnd.get(this.getMembers().size()));
	}

	public boolean forEachMember(Function<Player, Boolean> procedure)
	{
		for (Player player : this.getMembers())
		{
			if (!procedure.apply(player))
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean equals(Object obj)
	{
		return this == obj ? true : obj instanceof AbstractPlayerGroup && this.getLeaderObjectId() == ((AbstractPlayerGroup) obj).getLeaderObjectId();
	}
}
