package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class PartySmallWindowAll extends ServerPacket
{
	private final Party _party;
	private final Player _exclude;

	public PartySmallWindowAll(Player exclude, Party party)
	{
		this._exclude = exclude;
		this._party = party;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.PARTY_SMALL_WINDOW_ALL.writeId(this, buffer);
		buffer.writeInt(this._party.getLeaderObjectId());
		buffer.writeByte(this._party.getDistributionType().getId());
		buffer.writeByte(this._party.getMemberCount() - 1);

		for (Player member : this._party.getMembers())
		{
			if (member != null && member != this._exclude)
			{
				buffer.writeInt(member.getObjectId());
				buffer.writeString(member.getAppearance().getVisibleName());
				buffer.writeInt((int) member.getCurrentCp());
				buffer.writeInt(member.getMaxCp());
				buffer.writeInt((int) member.getCurrentHp());
				buffer.writeInt((int) member.getMaxHp());
				buffer.writeInt((int) member.getCurrentMp());
				buffer.writeInt(member.getMaxMp());
				buffer.writeInt(member.getVitalityPoints());
				buffer.writeByte(member.getLevel());
				buffer.writeShort(member.getPlayerClass().getId());
				buffer.writeByte(1);
				buffer.writeShort(member.getRace().ordinal());
				buffer.writeInt(0);
				Summon pet = member.getPet();
				buffer.writeInt(member.getServitors().size() + (pet != null ? 1 : 0));
				if (pet != null)
				{
					buffer.writeInt(pet.getObjectId());
					buffer.writeInt(pet.getId() + 1000000);
					buffer.writeByte(pet.getSummonType());
					buffer.writeString(pet.getName());
					buffer.writeInt((int) pet.getCurrentHp());
					buffer.writeInt((int) pet.getMaxHp());
					buffer.writeInt((int) pet.getCurrentMp());
					buffer.writeInt(pet.getMaxMp());
					buffer.writeByte(pet.getLevel());
				}

				member.getServitors().values().forEach(s -> {
					buffer.writeInt(s.getObjectId());
					buffer.writeInt(s.getId() + 1000000);
					buffer.writeByte(s.getSummonType());
					buffer.writeString(s.getName());
					buffer.writeInt((int) s.getCurrentHp());
					buffer.writeInt((int) s.getMaxHp());
					buffer.writeInt((int) s.getCurrentMp());
					buffer.writeInt(s.getMaxMp());
					buffer.writeByte(s.getLevel());
				});
			}
		}
	}
}
