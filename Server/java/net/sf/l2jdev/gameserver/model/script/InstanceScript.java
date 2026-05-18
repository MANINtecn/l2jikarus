package net.sf.l2jdev.gameserver.model.script;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.l2jdev.gameserver.managers.InstanceManager;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;
import net.sf.l2jdev.gameserver.model.instancezone.InstanceReenterType;
import net.sf.l2jdev.gameserver.model.instancezone.InstanceTemplate;
import net.sf.l2jdev.gameserver.network.NpcStringId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public abstract class InstanceScript extends Script
{
	private final Set<Integer> _templateIds = new HashSet<>();

	protected InstanceScript(int... templateIds)
	{
		if (templateIds.length == 0)
		{
			throw new IllegalStateException("No template ids were provided!");
		}
		for (int templateId : templateIds)
		{
			this._templateIds.add(templateId);
		}
	}

	public Set<Integer> getTemplateId()
	{
		return this._templateIds;
	}

	public boolean isInInstance(Instance instance)
	{
		return instance != null && this._templateIds.contains(instance.getTemplateId());
	}

	public Instance getPlayerInstance(Player player)
	{
		return InstanceManager.getInstance().getPlayerInstance(player, false);
	}

	public void showOnScreenMsg(Instance instance, NpcStringId npcStringId, int position, int timeInMilliseconds, String... parameters)
	{
		instance.broadcastPacket(new ExShowScreenMessage(npcStringId, position, timeInMilliseconds, parameters));
	}

	public void showOnScreenMsg(Instance instance, NpcStringId npcStringId, int position, int timeInMilliseconds, boolean showVisualEffect, String... parameters)
	{
		instance.broadcastPacket(new ExShowScreenMessage(npcStringId, position, timeInMilliseconds, showVisualEffect, parameters));
	}

	protected void enterInstance(Player player, Npc npc, int templateId)
	{
		Instance instance = this.getPlayerInstance(player);
		if (instance != null)
		{
			if (instance.getTemplateId() != templateId)
			{
				player.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_ENTER_AS_C1_IS_IN_ANOTHER_INSTANCE_ZONE).addString(player.getName()));
				return;
			}

			this.onEnter(player, instance, false);
		}
		else
		{
			InstanceManager instanceManager = InstanceManager.getInstance();
			InstanceTemplate instanceTemplate = instanceManager.getInstanceTemplate(templateId);
			if (instanceTemplate == null)
			{
				LOGGER.warning(player + " wants to create instance with unknown template id " + templateId + "!");
				return;
			}

			List<Player> group = instanceTemplate.getEnterGroup(player);
			if (group == null)
			{
				LOGGER.warning("Instance " + instanceTemplate.getName() + " (" + templateId + ") has invalid group size limits!");
				return;
			}

			if (!player.isGM() && (!instanceTemplate.validateConditions(group, npc, this::showHtmlFile) || !this.validateConditions(group, npc, instanceTemplate)))
			{
				return;
			}

			if (instanceTemplate.getMaxWorlds() != -1 && instanceManager.getWorldCount(templateId) >= instanceTemplate.getMaxWorlds())
			{
				player.sendPacket(SystemMessageId.THE_NUMBER_OF_INSTANCE_ZONES_THAT_CAN_BE_CREATED_HAS_BEEN_EXCEEDED_PLEASE_TRY_AGAIN_LATER);
				return;
			}

			for (Player member : group)
			{
				if (this.getPlayerInstance(member) != null)
				{
					group.forEach(p -> p.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_ENTER_AS_C1_IS_IN_ANOTHER_INSTANCE_ZONE).addString(member.getName())));
					return;
				}

				if (InstanceManager.getInstance().getInstanceTime(member, templateId) > 0L)
				{
					group.forEach(p -> p.sendPacket(new SystemMessage(SystemMessageId.C1_CANNOT_ENTER_YET).addString(member.getName())));
					return;
				}
			}

			instance = instanceManager.createInstance(instanceTemplate, player);

			for (Player member : group)
			{
				instance.addAllowed(member);
				this.onEnter(member, instance, true);
			}

			instanceTemplate.applyConditionEffects(group);
			if (instance.getReenterType() == InstanceReenterType.ON_ENTER)
			{
				instance.setReenterTime();
			}
		}
	}

	protected void onEnter(Player player, Instance instance, boolean isFirstEntry)
	{
		this.teleportPlayerIn(player, instance);
	}

	protected void teleportPlayerIn(Player player, Instance instance)
	{
		Location location = instance.getEnterLocation();
		if (location != null)
		{
			player.teleToLocation(location, instance);
		}
		else
		{
			LOGGER.warning("Missing start location for instance " + instance.getName() + " (" + instance.getId() + ")");
		}
	}

	protected void teleportPlayerOut(Player player, Instance instance)
	{
		instance.ejectPlayer(player);
	}

	protected void finishInstance(Player player)
	{
		Instance instance = player.getInstanceWorld();
		if (instance != null)
		{
			instance.finishInstance();
		}
	}

	protected void finishInstance(Player player, int delayInMinutes)
	{
		Instance instance = player.getInstanceWorld();
		if (instance != null)
		{
			instance.finishInstance(delayInMinutes);
		}
	}

	protected boolean validateConditions(List<Player> group, Npc npc, InstanceTemplate template)
	{
		return true;
	}
}
