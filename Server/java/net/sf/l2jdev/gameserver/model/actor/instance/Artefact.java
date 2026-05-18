package net.sf.l2jdev.gameserver.model.actor.instance;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;

public class Artefact extends Npc
{
	public Artefact(NpcTemplate template)
	{
		super(template);
		this.setInstanceType(InstanceType.Artefact);
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		Castle castle = this.getCastle();
		if (castle != null)
		{
			castle.registerArtefact(this);
		}
	}

	@Override
	public boolean isArtefact()
	{
		return true;
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}

	@Override
	public boolean canBeAttacked()
	{
		return false;
	}

	@Override
	public void onForcedAttack(Player player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill)
	{
	}

	@Override
	public void reduceCurrentHp(double value, Creature attacker, Skill skill, boolean isDOT, boolean directlyToHp, boolean critical, boolean reflect)
	{
	}
}
