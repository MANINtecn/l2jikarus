package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Set;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.holders.EnchantSkillHolder;
import net.sf.l2jdev.gameserver.data.xml.EnchantSkillGroupsData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.skill.enums.SkillEnchantType;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExEnchantSkillInfoDetail extends ServerPacket
{
	private final SkillEnchantType _type;
	private final int _skillId;
	private final int _skillLevel;
	private final int _skillSubLevel;
	private final EnchantSkillHolder _enchantSkillHolder;

	public ExEnchantSkillInfoDetail(SkillEnchantType type, int skillId, int skillLevel, int skillSubLevel, Player player)
	{
		this._type = type;
		this._skillId = skillId;
		this._skillLevel = skillLevel;
		this._skillSubLevel = skillSubLevel;
		this._enchantSkillHolder = EnchantSkillGroupsData.getInstance().getEnchantSkillHolder(skillSubLevel % 1000);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ENCHANT_SKILL_INFO_DETAIL.writeId(this, buffer);
		buffer.writeInt(this._type.ordinal());
		buffer.writeInt(this._skillId);
		buffer.writeShort(this._skillLevel);
		buffer.writeShort(this._skillSubLevel);
		if (this._enchantSkillHolder != null)
		{
			buffer.writeLong(this._enchantSkillHolder.getSp(this._type));
			buffer.writeInt(this._enchantSkillHolder.getChance(this._type));
			Set<ItemHolder> holders = this._enchantSkillHolder.getRequiredItems(this._type);
			buffer.writeInt(holders.size());
			holders.forEach(holder -> {
				buffer.writeInt(holder.getId());
				buffer.writeInt((int) holder.getCount());
			});
		}
	}
}
