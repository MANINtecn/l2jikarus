package net.sf.l2jdev.gameserver.network.clientpackets;

import java.util.Set;

import net.sf.l2jdev.gameserver.data.xml.EnchantSkillGroupsData;
import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.serverpackets.ExEnchantSkillInfo;

public class RequestExEnchantSkillInfo extends ClientPacket
{
	private int _skillId;
	private int _skillLevel;
	private int _skillSubLevel;

	@Override
	protected void readImpl()
	{
		this._skillId = this.readInt();
		this._skillLevel = this.readShort();
		this._skillSubLevel = this.readShort();
	}

	@Override
	protected void runImpl()
	{
		if (this._skillId > 0 && this._skillLevel > 0 && this._skillSubLevel >= 0)
		{
			Player player = this.getPlayer();
			if (player != null)
			{
				int replacedSkillId = player.getReplacementSkill(this._skillId);
				Skill skill = SkillData.getInstance().getSkill(replacedSkillId, this._skillLevel, this._skillSubLevel);
				if (skill != null)
				{
					Set<Integer> route = EnchantSkillGroupsData.getInstance().getRouteForSkill(this._skillId, this._skillLevel);
					if (!route.isEmpty())
					{
						Skill playerSkill = player.getKnownSkill(replacedSkillId);
						if (playerSkill != null && playerSkill.getLevel() == this._skillLevel && playerSkill.getSubLevel() == this._skillSubLevel)
						{
							player.sendPacket(new ExEnchantSkillInfo(this._skillId, this._skillLevel, this._skillSubLevel, playerSkill.getSubLevel()));
						}
					}
				}
			}
		}
	}
}
