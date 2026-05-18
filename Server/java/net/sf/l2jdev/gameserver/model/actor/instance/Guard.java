package net.sf.l2jdev.gameserver.model.actor.instance;

import net.sf.l2jdev.gameserver.ai.Intention;
import net.sf.l2jdev.gameserver.config.custom.FactionSystemConfig;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Attackable;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.InstanceType;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.npc.OnNpcFirstTalk;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;

public class Guard extends Attackable
{
	public Guard(NpcTemplate template)
	{
		super(template);
		this.setInstanceType(InstanceType.Guard);
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		if (attacker.isMonster() && !attacker.isFakePlayer())
		{
			return true;
		}
		if (FactionSystemConfig.FACTION_SYSTEM_ENABLED && FactionSystemConfig.FACTION_GUARDS_ENABLED && attacker.isPlayable())
		{
			Player player = attacker.asPlayer();
			if (player.isGood() && this.getTemplate().isClan(FactionSystemConfig.FACTION_EVIL_TEAM_NAME) || player.isEvil() && this.getTemplate().isClan(FactionSystemConfig.FACTION_GOOD_TEAM_NAME))
			{
				return true;
			}
		}

		return super.isAutoAttackable(attacker);
	}

	@Override
	public void addDamage(Creature attacker, int damage, Skill skill)
	{
		super.addDamage(attacker, damage, skill);
		this.getAI().startFollow(attacker);
		this.addDamageHate(attacker, 0L, 10L);
		World.getInstance().forEachVisibleObjectInRange(this, Guard.class, 500, guard -> {
			guard.getAI().startFollow(attacker);
			guard.addDamageHate(attacker, 0L, 10L);
		});
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		this.setRandomWalking(this.getTemplate().isRandomWalkEnabled());
		this.getAI().setIntention(Intention.ACTIVE);
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

		return "data/html/guard/" + pom + ".htm";
	}

	@Override
	public void onAction(Player player, boolean interactValue)
	{
		if (this.canTarget(player))
		{
			boolean interact = interactValue;
			if (FactionSystemConfig.FACTION_SYSTEM_ENABLED && FactionSystemConfig.FACTION_GUARDS_ENABLED && (player.isGood() && this.getTemplate().isClan(FactionSystemConfig.FACTION_EVIL_TEAM_NAME) || player.isEvil() && this.getTemplate().isClan(FactionSystemConfig.FACTION_GOOD_TEAM_NAME)))
			{
				interact = false;
				player.getAI().setIntention(Intention.ATTACK, this);
			}

			if (this.isFakePlayer() && this.isInCombat())
			{
				interact = false;
				player.getAI().setIntention(Intention.ATTACK, this);
			}

			if (this.getObjectId() != player.getTargetId())
			{
				player.setTarget(this);
			}
			else if (interact)
			{
				if (this.isInAggroList(player))
				{
					player.getAI().setIntention(Intention.ATTACK, this);
				}
				else if (!this.canInteract(player))
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
						if (EventDispatcher.getInstance().hasListener(EventType.ON_NPC_FIRST_TALK, this))
						{
							EventDispatcher.getInstance().notifyEventAsync(new OnNpcFirstTalk(this, player), this);
						}
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
}
