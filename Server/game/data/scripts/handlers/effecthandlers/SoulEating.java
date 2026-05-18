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

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Playable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.effects.AbstractEffect;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayableExpChanged;
import net.sf.l2jdev.gameserver.model.events.listeners.ConsumerEventListener;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.enums.SoulType;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExSpawnEmitter;

/**
 * Soul Eating effect implementation.
 * @author UnAfraid
 */
public class SoulEating extends AbstractEffect
{
	private final SoulType _type;
	private final int _expNeeded;
	private final Double _maxSouls;

	public SoulEating(StatSet params)
	{
		_type = params.getEnum("type", SoulType.class, SoulType.LIGHT);
		_expNeeded = params.getInt("expNeeded");
		_maxSouls = params.getDouble("maxSouls");
	}

	@Override
	public void onStart(Creature effector, Creature effected, Skill skill, Item item)
	{
		if (effected.isPlayer())
		{
			effected.addListener(new ConsumerEventListener(effected, EventType.ON_PLAYABLE_EXP_CHANGED, (OnPlayableExpChanged event) -> onExperienceReceived(event.getPlayable(), (event.getNewExp() - event.getOldExp())), this));
		}
	}

	@Override
	public void onExit(Creature effector, Creature effected, Skill skill)
	{
		if (effected.isPlayer())
		{
			effected.removeListenerIf(EventType.ON_PLAYABLE_EXP_CHANGED, listener -> listener.getOwner() == this);
		}
	}

	@Override
	public void pump(Creature effected, Skill skill)
	{
		effected.getStat().mergeAdd(Stat.MAX_SOULS, _maxSouls);
	}

	private void onExperienceReceived(Playable playable, long exp)
	{
		// TODO: Verify logic.
		if (playable.isPlayer() && (exp >= _expNeeded))
		{
			final Player player = playable.asPlayer();
			final int maxSouls = (int) player.getStat().getValue(Stat.MAX_SOULS, 0);
			if (player.getChargedSouls(_type) >= maxSouls)
			{
				playable.sendPacket(SystemMessageId.YOU_CANNOT_ABSORB_MORE_SOULS);
				return;
			}

			player.increaseSouls(1, _type);

			if ((player.getTarget() != null) && player.getTarget().isNpc())
			{
				final Npc npc = playable.getTarget().asNpc();
				player.broadcastPacket(new ExSpawnEmitter(player, npc));
			}
		}
	}
}
