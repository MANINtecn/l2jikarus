package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.data.xml.SkillTreeData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.skill.CommonSkill;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExAlchemySkillList extends ServerPacket
{
	private final List<Skill> _skills = new ArrayList<>();

	public ExAlchemySkillList(Player player)
	{
		for (Skill s : player.getAllSkills())
		{
			if (SkillTreeData.getInstance().isAlchemySkill(s.getId(), s.getLevel()))
			{
				this._skills.add(s);
			}
		}

		this._skills.add(SkillData.getInstance().getSkill(CommonSkill.ALCHEMY_CUBE.getId(), 1));
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ALCHEMY_SKILL_LIST.writeId(this, buffer);
		buffer.writeInt(this._skills.size());

		for (Skill skill : this._skills)
		{
			buffer.writeInt(skill.getId());
			buffer.writeInt(skill.getLevel());
			buffer.writeLong(0L);
			buffer.writeByte(skill.getId() != CommonSkill.ALCHEMY_CUBE.getId());
		}
	}
}
