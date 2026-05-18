package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.LinkedList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.enums.SkillEnchantType;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExEnchantSkillList extends ServerPacket
{
	private final SkillEnchantType _type;
	private final List<Skill> _skills = new LinkedList<>();

	public ExEnchantSkillList(SkillEnchantType type)
	{
		this._type = type;
	}

	public void addSkill(Skill skill)
	{
		this._skills.add(skill);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ENCHANT_SKILL_LIST.writeId(this, buffer);
		buffer.writeInt(this._type.ordinal());
		buffer.writeInt(this._skills.size());

		for (Skill skill : this._skills)
		{
			buffer.writeInt(skill.getId());
			buffer.writeShort(skill.getLevel());
			buffer.writeShort(skill.getSubLevel());
		}
	}
}
