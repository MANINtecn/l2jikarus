package net.sf.l2jdev.gameserver.network.serverpackets.newskillenchant;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.holders.EnchantStarHolder;
import net.sf.l2jdev.gameserver.data.xml.SkillEnchantData;
import net.sf.l2jdev.gameserver.model.ItemInfo;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.AbstractItemPacket;

public class ExSkillEnchantInfo extends AbstractItemPacket
{
	private final Skill _skill;
	private final Player _player;
	private final EnchantStarHolder _starHolder;

	public ExSkillEnchantInfo(Skill skill, Player player)
	{
		this._skill = skill;
		this._player = player;
		this._starHolder = SkillEnchantData.getInstance().getEnchantStar(SkillEnchantData.getInstance().getSkillEnchant(skill.getId()).getStarLevel());
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SKILL_ENCHANT_INFO.writeId(this, buffer);
		buffer.writeInt(this._skill.getId());
		buffer.writeInt(this._skill.getSubLevel());
		buffer.writeInt(this._player.getSkillEnchantExp(this._starHolder.getLevel()));
		buffer.writeInt(this._starHolder.getExpMax());
		buffer.writeInt(SkillEnchantData.getInstance().getChanceEnchantMap(this._skill) * 100);
		buffer.writeShort(this.calculatePacketSize(new ItemInfo(new Item(57))));
		buffer.writeInt(57);
		buffer.writeLong(1000000L);
	}
}
