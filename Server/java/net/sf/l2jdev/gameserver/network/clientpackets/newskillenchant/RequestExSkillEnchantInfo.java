package net.sf.l2jdev.gameserver.network.clientpackets.newskillenchant;

import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.data.xml.SkillEnchantData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillEnchantHolder;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.newskillenchant.ExSkillEnchantInfo;

public class RequestExSkillEnchantInfo extends ClientPacket
{
	private int _skillId;
	private int _skillLevel;
	private int _skillSubLevel;

	@Override
	protected void readImpl()
	{
		this._skillId = this.readInt();
		this._skillLevel = this.readInt();
		this._skillSubLevel = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			SkillEnchantHolder skillEnchantHolder = SkillEnchantData.getInstance().getSkillEnchant(this._skillId);
			if (skillEnchantHolder == null)
			{
				PacketLogger.warning("Skill does not exist at SkillEnchantData id-" + this._skillId);
			}
			else
			{
				Skill enchantSkill = SkillData.getInstance().getSkill(this._skillId, this._skillLevel, this._skillSubLevel);
				if (enchantSkill != null)
				{
					player.sendPacket(new ExSkillEnchantInfo(enchantSkill, player));
				}
			}
		}
	}
}
