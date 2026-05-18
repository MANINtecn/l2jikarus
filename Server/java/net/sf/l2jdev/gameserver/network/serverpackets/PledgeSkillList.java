package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class PledgeSkillList extends ServerPacket
{
	private final Collection<Skill> _skills;
	private final Collection<PledgeSkillList.SubPledgeSkill> _subSkills;

	public PledgeSkillList(Clan clan)
	{
		this._skills = clan.getAllSkills();
		this._subSkills = clan.getAllSubSkills();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PLEDGE_SKILL_LIST.writeId(this, buffer);
		buffer.writeInt(this._skills.size());
		buffer.writeInt(this._subSkills.size());

		for (Skill sk : this._skills)
		{
			buffer.writeInt(sk.getDisplayId());
			buffer.writeShort(sk.getDisplayLevel());
			buffer.writeShort(0);
		}

		for (PledgeSkillList.SubPledgeSkill sk : this._subSkills)
		{
			buffer.writeInt(sk._subType);
			buffer.writeInt(sk._skillId);
			buffer.writeShort(sk._skillLevel);
			buffer.writeShort(0);
		}
	}

	public static class SubPledgeSkill
	{
		int _subType;
		int _skillId;
		int _skillLevel;

		public SubPledgeSkill(int subType, int skillId, int skillLevel)
		{
			this._subType = subType;
			this._skillId = skillId;
			this._skillLevel = skillLevel;
		}
	}
}
