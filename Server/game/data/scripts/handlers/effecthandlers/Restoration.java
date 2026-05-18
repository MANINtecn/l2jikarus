/*
 * Copyright (c) 2013 L2jBAN-JDEV
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package handlers.effecthandlers;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.effects.EffectType;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.pet.PetItemList;

/**
 * Restoration effect implementation.
 * @author Zoey76, BAN-JDEV
 */
public class Restoration extends AbstractEffect
{
	private final int _itemId;
	private final int _itemCount;
	private final int _itemEnchantmentLevel;

	public Restoration(StatSet params)
	{
		_itemId = params.getInt("itemId", 0);
		_itemCount = params.getInt("itemCount", 0);
		_itemEnchantmentLevel = params.getInt("itemEnchantmentLevel", 0);
	}

	@Override
	public boolean isInstant()
	{
		return true;
	}

	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		if (!effected.isPlayable())
		{
			return;
		}

		if ((_itemId <= 0) || (_itemCount <= 0))
		{
			effected.sendPacket(SystemMessageId.FAILED_TO_CHANGE_THE_ITEM);
			LOGGER.warning(Restoration.class.getSimpleName() + " effect with wrong item Id/count: " + _itemId + "/" + _itemCount + "!");
			return;
		}

		if (effected.isPlayer())
		{
			final Item newItem = effected.asPlayer().addItem(ItemProcessType.REWARD, _itemId, _itemCount, effector, true);
			if (_itemEnchantmentLevel > 0)
			{
				newItem.setEnchantLevel(_itemEnchantmentLevel);
			}
		}
		else if (effected.isPet())
		{
			final Player target = effected.asPlayer();
			final Item newItem = effected.getInventory().addItem(ItemProcessType.REWARD, _itemId, _itemCount, target, effector);
			if (_itemEnchantmentLevel > 0)
			{
				newItem.setEnchantLevel(_itemEnchantmentLevel);
			}

			target.sendPacket(new PetItemList(effected.getInventory().getItems()));
		}
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.EXTRACT_ITEM;
	}
}
