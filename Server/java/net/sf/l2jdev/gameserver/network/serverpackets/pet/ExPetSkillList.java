package net.sf.l2jdev.gameserver.network.serverpackets.pet;

import java.util.Collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.instance.Pet;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPetSkillList extends ServerPacket
{
	private final boolean _onEnter;
	private final Pet _pet;
	private final Collection<Skill> _skills;

	public ExPetSkillList(boolean onEnter, Pet pet)
	{
		this._onEnter = onEnter;
		this._pet = pet;
		this._skills = pet.getAllSkills();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PET_SKILL_LIST.writeId(this, buffer);
		buffer.writeByte(this._onEnter);
		buffer.writeInt(this._skills.size());

		for (Skill sk : this._skills)
		{
			int skillId = this._pet.getReplacementSkill(sk.getDisplayId());
			buffer.writeInt(skillId);
			buffer.writeInt(sk.getDisplayLevel());
			buffer.writeInt(sk.getReuseDelayGroup());
			buffer.writeByte(0);
			buffer.writeByte(0);
		}
	}
}
