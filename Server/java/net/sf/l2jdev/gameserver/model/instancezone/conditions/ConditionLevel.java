package net.sf.l2jdev.gameserver.model.instancezone.conditions;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.instancezone.InstanceTemplate;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class ConditionLevel extends Condition
{
	private final int _min;
	private final int _max;

	public ConditionLevel(InstanceTemplate template, StatSet parameters, boolean onlyLeader, boolean showMessageAndHtml)
	{
		super(template, parameters, onlyLeader, showMessageAndHtml);
		this._min = Math.min(PlayerConfig.PLAYER_MAXIMUM_LEVEL, parameters.getInt("min", 1));
		this._max = Math.min(PlayerConfig.PLAYER_MAXIMUM_LEVEL, parameters.getInt("max", Integer.MAX_VALUE));
		this.setSystemMessage(SystemMessageId.C1_DOES_NOT_MEET_LEVEL_REQUIREMENTS_AND_CANNOT_ENTER, (msg, player) -> msg.addString(player.getName()));
	}

	@Override
	protected boolean test(Player player, Npc npc)
	{
		return player.getLevel() >= this._min && player.getLevel() <= this._max;
	}
}
