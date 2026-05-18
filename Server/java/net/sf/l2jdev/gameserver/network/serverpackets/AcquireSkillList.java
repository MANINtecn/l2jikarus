package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.xml.SkillTreeData;
import net.sf.l2jdev.gameserver.model.SkillLearn;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class AcquireSkillList extends ServerPacket
{
	private Player _player;
	private Collection<SkillLearn> _learnable;

	public AcquireSkillList(Player player)
	{
		if (!player.isSubclassLocked())
		{
			this._player = player;
			if (player.isTransformed())
			{
				this._learnable = Collections.emptyList();
			}
			else
			{
				this._learnable = SkillTreeData.getInstance().getAvailableSkills(player, player.getPlayerClass(), false, false);
				this._learnable.addAll(SkillTreeData.getInstance().getNextAvailableSkills(player, player.getPlayerClass(), false, false));
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		if (this._player != null)
		{
			ServerPackets.ACQUIRE_SKILL_LIST.writeId(this, buffer);
			buffer.writeShort(this._learnable.size());

			for (SkillLearn skill : this._learnable)
			{
				int skillId = this._player.getReplacementSkill(skill.getSkillId());
				buffer.writeInt(skillId);
				buffer.writeInt(skill.getSkillLevel());
				buffer.writeLong(skill.getLevelUpSp());
				buffer.writeByte(skill.getGetLevel());
				buffer.writeByte(0);
				buffer.writeByte(this._player.getKnownSkill(skillId) == null);
				buffer.writeByte(skill.getRequiredItems().size());

				for (List<ItemHolder> item : skill.getRequiredItems())
				{
					buffer.writeInt(item.get(0).getId());
					buffer.writeLong(item.get(0).getCount());
				}

				List<Skill> removeSkills = new LinkedList<>();

				for (int id : skill.getRemoveSkills())
				{
					Skill removeSkill = this._player.getKnownSkill(id);
					if (removeSkill != null)
					{
						removeSkills.add(removeSkill);
					}
				}

				buffer.writeByte(removeSkills.size());

				for (Skill removed : removeSkills)
				{
					buffer.writeInt(removed.getId());
					buffer.writeInt(removed.getLevel());
				}
			}
		}
	}
}
