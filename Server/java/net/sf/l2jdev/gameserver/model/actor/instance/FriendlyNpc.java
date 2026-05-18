package net.sf.l2jdev.gameserver.model.actor.instance;

import net.sf.l2jdev.gameserver.ai.CreatureAI;
import net.sf.l2jdev.gameserver.ai.FriendlyNpcAI;
import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.model.actor.Attackable;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnAttackableAttack;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnAttackableKill;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcFirstTalk;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;

public class FriendlyNpc extends Attackable
{
	private boolean _isAutoAttackable = true;

	public FriendlyNpc(NpcTemplate template)
	{
		super(template);
		this.setInstanceType(InstanceType.FriendlyNpc);
		this.setCanReturnToSpawnPoint(false);
	}

	@Override
	public boolean isAttackable()
	{
		return false;
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return this._isAutoAttackable && !attacker.isPlayable() && !(attacker instanceof FriendlyNpc);
	}

	@Override
	public void setAutoAttackable(boolean value)
	{
		this._isAutoAttackable = value;
	}

	@Override
	public void addDamage(Creature attacker, int damage, Skill skill)
	{
		if (!attacker.isPlayable() && !(attacker instanceof FriendlyNpc))
		{
			super.addDamage(attacker, damage, skill);
		}

		if (attacker.isAttackable() && EventDispatcher.getInstance().hasListener(EventType.ON_ATTACKABLE_ATTACK, this))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnAttackableAttack(null, this, damage, skill, false), this);
		}
	}

	@Override
	public void addDamageHate(Creature attacker, long damage, long aggro)
	{
		if (!attacker.isPlayable() && !(attacker instanceof FriendlyNpc))
		{
			super.addDamageHate(attacker, damage, aggro);
		}
	}

	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		if (killer != null && killer.isAttackable() && EventDispatcher.getInstance().hasListener(EventType.ON_ATTACKABLE_KILL, this))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnAttackableKill(null, this, false), this);
		}

		return true;
	}

	@Override
	public void onAction(Player player, boolean interact)
	{
		if (this.canTarget(player))
		{
			if (this.getObjectId() != player.getTargetId())
			{
				player.setTarget(this);
			}
			else if (interact)
			{
				if (!this.canInteract(player))
				{
					player.getAI().setIntention(Intention.INTERACT, this);
				}
				else
				{
					player.setLastFolkNPC(this);
					if (this.hasListener(EventType.ON_NPC_QUEST_START))
					{
						player.setLastQuestNpcObject(this.getObjectId());
					}

					if (this.hasListener(EventType.ON_NPC_FIRST_TALK))
					{
						EventDispatcher.getInstance().notifyEventAsync(new OnNpcFirstTalk(this, player), this);
					}
					else
					{
						this.showChatWindow(player, 0);
					}
				}
			}

			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	@Override
	public String getHtmlPath(int npcId, int value, Player player)
	{
		String pom = "";
		if (value == 0)
		{
			pom = Integer.toString(npcId);
		}
		else
		{
			pom = npcId + "-" + value;
		}

		return "data/html/default/" + pom + ".htm";
	}

	@Override
	protected CreatureAI initAI()
	{
		return new FriendlyNpcAI(this);
	}
}
