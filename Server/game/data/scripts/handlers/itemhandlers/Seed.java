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

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.handler.IItemHandler;
import net.sf.l2jdev.gameserver.managers.CastleManorManager;
import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Playable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.instance.Chest;
import net.sf.l2jdev.gameserver.model.actor.instance.Monster;
import net.sf.l2jdev.gameserver.model.item.enums.ItemSkillType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemSkillHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;

/**
 * @author l3x
 */
public class Seed implements IItemHandler
{
	@Override
	public boolean onItemUse(Playable playable, Item item, boolean forceUse)
	{
		if (!GeneralConfig.ALLOW_MANOR)
		{
			return false;
		}
		else if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.THIS_ITEM_CANNOT_BE_TRANSFERRED_TO_A_GUARDIAN);
			return false;
		}

		final WorldObject tgt = playable.getTarget();
		if ((tgt == null) || !tgt.isNpc())
		{
			playable.sendPacket(SystemMessageId.INVALID_TARGET);
			return false;
		}
		else if (!tgt.isMonster() || tgt.asMonster().isRaid() || (tgt instanceof Chest))
		{
			playable.sendPacket(SystemMessageId.THE_TARGET_IS_UNAVAILABLE_FOR_SEEDING);
			return false;
		}

		final Monster target = tgt.asMonster();
		if (target.isDead())
		{
			playable.sendPacket(SystemMessageId.INVALID_TARGET);
			return false;
		}
		else if (target.isSeeded())
		{
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		final net.sf.l2jdev.gameserver.model.Seed seed = CastleManorManager.getInstance().getSeed(item.getId());
		if (seed == null)
		{
			return false;
		}

		final Castle taxCastle = target.getTaxCastle();
		if ((taxCastle == null) || (seed.getCastleId() != taxCastle.getResidenceId()))
		{
			playable.sendPacket(SystemMessageId.THIS_SEED_MAY_NOT_BE_SOWN_HERE);
			return false;
		}

		final Player player = playable.asPlayer();
		target.setSeeded(seed, player);

		final List<ItemSkillHolder> skills = item.getTemplate().getSkills(ItemSkillType.NORMAL);
		if (skills != null)
		{
			skills.forEach(holder -> player.useMagic(holder.getSkill(), item, false, false));
		}

		return true;
	}
}
