package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.Summon;
import net.sf.l2jdev.gameserver.model.skill.AbnormalType;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.enums.SkillFinishType;

public class RequestDispel extends ClientPacket
{
	private int _objectId;
	private int _skillId;
	private int _skillLevel;
	private int _skillSubLevel;

	@Override
	protected void readImpl()
	{
		this._objectId = this.readInt();
		this._skillId = this.readInt();
		this._skillLevel = this.readShort();
		this._skillSubLevel = this.readShort();
	}

	@Override
	protected void runImpl()
	{
		if (this._skillId > 0 && this._skillLevel > 0)
		{
			Player player = this.getPlayer();
			if (player != null)
			{
				Skill skill = SkillData.getInstance().getSkill(this._skillId, this._skillLevel, this._skillSubLevel);
				if (skill != null)
				{
					if (skill.canBeDispelled() && !skill.isDebuff())
					{
						if (skill.getAbnormalType() != AbnormalType.TRANSFORM)
						{
							if (!skill.isDance() || PlayerConfig.DANCE_CANCEL_BUFF)
							{
								if (player.getObjectId() == this._objectId)
								{
									player.stopSkillEffects(SkillFinishType.REMOVED, this._skillId);
								}
								else
								{
									Summon pet = player.getPet();
									if (pet != null && pet.getObjectId() == this._objectId)
									{
										pet.stopSkillEffects(SkillFinishType.REMOVED, this._skillId);
									}

									Summon servitor = player.getServitor(this._objectId);
									if (servitor != null)
									{
										servitor.stopSkillEffects(SkillFinishType.REMOVED, this._skillId);
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
