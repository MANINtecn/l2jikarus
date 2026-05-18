package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class PartyMemberPosition extends ServerPacket
{
	private final Map<Integer, Location> locations = new HashMap<>();

	public PartyMemberPosition(Party party)
	{
		this.reuse(party);
	}

	public void reuse(Party party)
	{
		this.locations.clear();

		for (Player member : party.getMembers())
		{
			if (member != null)
			{
				this.locations.put(member.getObjectId(), member.getLocation());
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.PARTY_MEMBER_POSITION.writeId(this, buffer);
		buffer.writeInt(this.locations.size());

		for (Entry<Integer, Location> entry : this.locations.entrySet())
		{
			Location loc = entry.getValue();
			buffer.writeInt(entry.getKey());
			buffer.writeInt(loc.getX());
			buffer.writeInt(loc.getY());
			buffer.writeInt(loc.getZ());
		}
	}
}
