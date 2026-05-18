package net.sf.l2jdev.gameserver.network.clientpackets.newskillenchant;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.gameserver.data.holders.EnchantItemExpHolder;
import net.sf.l2jdev.gameserver.data.holders.EnchantStarHolder;
import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.data.xml.SkillEnchantData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillEnchantHolder;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.newskillenchant.ExSkillEnchantCharge;
import net.sf.l2jdev.gameserver.network.serverpackets.newskillenchant.ExSkillEnchantInfo;

public class RequestExSkillEnchantCharge extends ClientPacket
{
	private int _skillId;
	private int _skillLevel;
	private int _skillSubLevel;
	private final List<ItemHolder> _itemList = new ArrayList<>();

	@Override
	protected void readImpl()
	{
		this._skillId = this.readInt();
		this._skillLevel = this.readInt();
		this._skillSubLevel = this.readInt();
		int size = this.readInt();

		for (int i = 0; i < size; i++)
		{
			int objectId = this.readInt();
			long count = this.readLong();
			if (count > 0L)
			{
				this._itemList.add(new ItemHolder(objectId, count));
			}
		}
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Skill skill = SkillData.getInstance().getSkill(this._skillId, this._skillLevel, this._skillSubLevel);
			if (skill == null)
			{
				return;
			}

			SkillEnchantHolder skillEnchantHolder = SkillEnchantData.getInstance().getSkillEnchant(skill.getId());
			if (skillEnchantHolder == null)
			{
				return;
			}

			EnchantStarHolder starHolder = SkillEnchantData.getInstance().getEnchantStar(skillEnchantHolder.getStarLevel());
			if (starHolder == null)
			{
				return;
			}

			int curExp = player.getSkillEnchantExp(starHolder.getLevel());
			long feeAdena = 0L;

			for (ItemHolder itemCharge : this._itemList)
			{
				Item item = player.getInventory().getItemByObjectId(itemCharge.getId());
				if (item == null)
				{
					PacketLogger.warning(this.getClass().getSimpleName() + " Player" + player.getName() + " trying charge skill exp enchant with not exist item by objectId - " + itemCharge.getId());
				}
				else
				{
					EnchantItemExpHolder itemExpHolder = SkillEnchantData.getInstance().getEnchantItem(starHolder.getLevel(), item.getId());
					if (itemExpHolder != null)
					{
						feeAdena = itemCharge.getCount() * starHolder.getFeeAdena();
						if (player.getAdena() < feeAdena)
						{
							player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA);
							player.sendPacket(new ExSkillEnchantCharge(skill.getId(), 0));
							return;
						}

						if (itemExpHolder.getStarLevel() <= starHolder.getLevel())
						{
							curExp = (int) (curExp + itemExpHolder.getExp() * itemCharge.getCount());
							player.destroyItem(ItemProcessType.FEE, item, itemCharge.getCount(), null, true);
						}
						else
						{
							PacketLogger.warning(this.getClass().getSimpleName() + " Player" + player.getName() + " trying charge item with not support star level skillstarLevel-" + starHolder.getLevel() + " itemStarLevel-" + itemExpHolder.getStarLevel() + " itemId-" + itemExpHolder.getId());
						}
					}
					else
					{
						PacketLogger.warning(this.getClass().getSimpleName() + " Player" + player.getName() + " trying charge skill with missed item on XML  itemId-" + item.getId());
					}
				}
			}

			player.setSkillEnchantExp(starHolder.getLevel(), Math.min(starHolder.getExpMax(), curExp));
			player.reduceAdena(ItemProcessType.FEE, feeAdena, null, true);
			player.sendPacket(new ExSkillEnchantCharge(skill.getId(), 0));
			player.sendPacket(new ExSkillEnchantInfo(skill, player));
		}
	}
}
