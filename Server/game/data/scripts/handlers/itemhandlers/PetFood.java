/*
 * This file is part of the L2J BAN-JDEV project.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.itemhandlers;

import java.util.List;
import java.util.Set;

import net.sf.l2jdev.gameserver.data.xml.PetDataTable;
import net.sf.l2jdev.gameserver.data.xml.SkillData;
import net.sf.l2jdev.gameserver.handler.IItemHandler;
import net.sf.l2jdev.gameserver.model.actor.Playable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.instance.Pet;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.enums.ItemSkillType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemSkillHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Kerberos, Zoey76
 */
public class PetFood implements IItemHandler
{
	@Override
	public boolean onItemUse(Playable playable, Item item, boolean forceUse)
	{
		if (playable.isPet() && !playable.asPet().canEatFoodId(item.getId()))
		{
			playable.sendPacket(SystemMessageId.THE_GUARDIAN_CANNOT_USE_THIS_ITEM);
			return false;
		}

		final List<ItemSkillHolder> skills = item.getTemplate().getSkills(ItemSkillType.NORMAL);
		if (skills != null)
		{
			skills.forEach(holder -> useFood(playable, holder.getSkillId(), holder.getSkillLevel(), item));
		}

		return true;
	}

	private static boolean useFood(Playable activeChar, int skillId, int skillLevel, Item item)
	{
		final Skill skill = SkillData.getInstance().getSkill(skillId, skillLevel);
		if (skill != null)
		{
			if (activeChar.isPet())
			{
				final Pet pet = activeChar.asPet();
				if (pet.destroyItem(ItemProcessType.NONE, item.getObjectId(), 1, null, false))
				{
					pet.broadcastSkillPacket(new MagicSkillUse(pet, pet, skillId, skillLevel, 0, 0), pet);
					skill.applyEffects(pet, pet);
					pet.broadcastStatusUpdate();
					if (pet.isHungry())
					{
						pet.sendPacket(SystemMessageId.YOUR_GUARDIAN_HAS_EATEN_A_LITTLE_BUT_IS_STILL_HUNGRY);
					}

					return true;
				}
			}
			else if (activeChar.isPlayer())
			{
				final Player player = activeChar.asPlayer();
				if (player.isMounted())
				{
					final Set<Integer> foodIds = PetDataTable.getInstance().getPetData(player.getMountNpcId()).getFood();
					if (foodIds.contains(item.getId()) && player.destroyItem(ItemProcessType.NONE, item.getObjectId(), 1, null, false))
					{
						player.broadcastSkillPacket(new MagicSkillUse(player, player, skillId, skillLevel, 0, 0), player);
						skill.applyEffects(player, player);
						return true;
					}
				}

				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_THE_REQUIREMENTS_ARE_NOT_MET);
				sm.addItemName(item);
				player.sendPacket(sm);
			}
		}

		return false;
	}
}
