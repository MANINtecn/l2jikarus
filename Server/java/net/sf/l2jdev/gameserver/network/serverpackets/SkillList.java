package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class SkillList extends ServerPacket
{
	private final List<SkillList.Skill> _skills = new ArrayList<>();
	private int _lastLearnedSkillId = 0;

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.SKILL_LIST.writeId(this, buffer);
		this._skills.sort(Comparator.comparing(s -> SkillData.getInstance().getSkill(s.id, s.level, s.subLevel).isToggle() ? 1 : 0));
		buffer.writeInt(this._skills.size());

		for (SkillList.Skill temp : this._skills)
		{
			buffer.writeInt(temp.passive);
			buffer.writeShort(temp.level);
			buffer.writeShort(temp.subLevel);
			buffer.writeInt(temp.id);
			buffer.writeInt(temp.reuseDelayGroup);
			buffer.writeByte(temp.disabled);
			buffer.writeByte(temp.enchanted);
		}

		buffer.writeInt(this._lastLearnedSkillId);
	}

	public void addSkill(int id, int reuseDelayGroup, int level, int subLevel, boolean passive, boolean disabled, boolean enchanted)
	{
		this._skills.add(new SkillList.Skill(id, reuseDelayGroup, level, subLevel, passive, disabled, enchanted));
	}

	public void setLastLearnedSkillId(int lastLearnedSkillId)
	{
		this._lastLearnedSkillId = lastLearnedSkillId;
	}

	private static class Skill
	{
		public int id;
		public int reuseDelayGroup;
		public int level;
		public int subLevel;
		public boolean passive;
		public boolean disabled;
		public boolean enchanted;

		Skill(int pId, int pReuseDelayGroup, int pLevel, int pSubLevel, boolean pPassive, boolean pDisabled, boolean pEnchanted)
		{
			this.id = pId;
			this.reuseDelayGroup = pReuseDelayGroup;
			this.level = pLevel;
			this.subLevel = pSubLevel;
			this.passive = pPassive;
			this.disabled = pDisabled;
			this.enchanted = pEnchanted;
		}
	}
}
