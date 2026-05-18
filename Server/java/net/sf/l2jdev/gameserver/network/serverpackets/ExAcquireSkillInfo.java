package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.LinkedList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.SkillLearn;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExAcquireSkillInfo extends ServerPacket
{
	private final Player _player;
	private final int _id;
	private final int _level;
	private final int _dualClassLevel;
	private final long _spCost;
	private final int _minLevel;
	private final List<List<ItemHolder>> _itemReq;
	private final List<Skill> _skillRem = new LinkedList<>();

	public ExAcquireSkillInfo(Player player, SkillLearn skillLearn)
	{
		this._player = player;
		this._id = skillLearn.getSkillId();
		this._level = skillLearn.getSkillLevel();
		this._dualClassLevel = skillLearn.getDualClassLevel();
		this._spCost = skillLearn.getLevelUpSp();
		this._minLevel = skillLearn.getGetLevel();
		this._itemReq = skillLearn.getRequiredItems();

		for (int id : skillLearn.getRemoveSkills())
		{
			Skill removeSkill = player.getKnownSkill(id);
			if (removeSkill != null)
			{
				this._skillRem.add(removeSkill);
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ACQUIRE_SKILL_INFO.writeId(this, buffer);
		buffer.writeInt(this._player.getReplacementSkill(this._id));
		buffer.writeInt(this._level);
		buffer.writeLong(this._spCost);
		buffer.writeShort(this._minLevel);
		buffer.writeShort(this._dualClassLevel);
		buffer.writeInt(this._itemReq.size());

		for (List<ItemHolder> holder : this._itemReq)
		{
			ItemHolder first = holder.get(0);
			buffer.writeInt(first.getId());
			buffer.writeLong(first.getCount());
		}

		buffer.writeInt(this._skillRem.size());

		for (Skill skill : this._skillRem)
		{
			buffer.writeInt(skill.getId());
			buffer.writeInt(skill.getLevel());
		}
	}
}
