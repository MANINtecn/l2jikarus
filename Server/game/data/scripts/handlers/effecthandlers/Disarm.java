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
package handlers.effecthandlers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.effects.EffectFlag;
import net.sf.l2jdev.gameserver.model.item.enums.BodyPart;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.Inventory;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;

/**
 * Disarm effect implementation.
 * @author nBd
 */
public class Disarm extends AbstractEffect
{
	private static final Map<Integer, Integer> _disarmedPlayers = new ConcurrentHashMap<>();

	public Disarm(StatSet params)
	{
	}

	@Override
	public boolean canStart(Creature effector, Creature effected, Skill skill)
	{
		return effected.isPlayer();
	}

	@Override
	public long getEffectFlags()
	{
		return EffectFlag.DISARMED.getMask();
	}

	@Override
	public void onStart(Creature effector, Creature effected, Skill skill, Item item)
	{
		final Player player = effected.asPlayer();
		if (player == null)
		{
			return;
		}

		final Item itemToDisarm = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if (itemToDisarm == null)
		{
			return;
		}

		final BodyPart bodyPart = BodyPart.fromItem(itemToDisarm);
		player.getInventory().unEquipItemInBodySlot(bodyPart);

		final InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(itemToDisarm);
		player.sendInventoryUpdate(iu);
		player.broadcastUserInfo();

		_disarmedPlayers.put(player.getObjectId(), itemToDisarm.getObjectId());
	}

	@Override
	public void onExit(Creature effector, Creature effected, Skill skill)
	{
		final Player player = effected.asPlayer();
		if (player == null)
		{
			return;
		}

		final Integer itemObjectId = _disarmedPlayers.remove(effected.getObjectId());
		if (itemObjectId == null)
		{
			return;
		}

		final Item item = player.getInventory().getItemByObjectId(itemObjectId);
		if (item == null)
		{
			return;
		}

		player.getInventory().equipItem(item);
		final InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(item);
		player.sendInventoryUpdate(iu);
	}
}
