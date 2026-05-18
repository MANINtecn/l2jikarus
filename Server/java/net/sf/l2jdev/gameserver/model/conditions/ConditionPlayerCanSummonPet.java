package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.sql.CharSummonTable;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class ConditionPlayerCanSummonPet extends Condition
{
	private final boolean _value;

	public ConditionPlayerCanSummonPet(boolean value)
	{
		this._value = value;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		Player player = effector.asPlayer();
		if (player == null)
		{
			return false;
		}
		boolean canSummon = true;
		if (PlayerConfig.RESTORE_PET_ON_RECONNECT && CharSummonTable.getInstance().getPets().containsKey(player.getObjectId()))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_MULTIPLE_GUARDIANS_AT_THE_SAME_TIME);
			canSummon = false;
		}
		else if (player.hasPet())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_MULTIPLE_GUARDIANS_AT_THE_SAME_TIME);
			canSummon = false;
		}
		else if (player.isFlyingMounted() || player.isMounted() || player.inObserverMode() || player.isTeleporting())
		{
			canSummon = false;
		}

		return this._value == canSummon;
	}
}
