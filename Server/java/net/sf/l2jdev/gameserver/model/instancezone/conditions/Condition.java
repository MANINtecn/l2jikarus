package net.sf.l2jdev.gameserver.model.instancezone.conditions;

import java.util.List;
import java.util.function.BiConsumer;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.instancezone.InstanceTemplate;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public abstract class Condition
{
	private final InstanceTemplate _template;
	private final StatSet _parameters;
	private final boolean _leaderOnly;
	private final boolean _showMessageAndHtml;
	private SystemMessageId _systemMsg = null;
	private BiConsumer<SystemMessage, Player> _systemMsgParams = null;

	public Condition(InstanceTemplate template, StatSet parameters, boolean onlyLeader, boolean showMessageAndHtml)
	{
		this._template = template;
		this._parameters = parameters;
		this._leaderOnly = onlyLeader;
		this._showMessageAndHtml = showMessageAndHtml;
	}

	protected final StatSet getParameters()
	{
		return this._parameters;
	}

	public InstanceTemplate getInstanceTemplate()
	{
		return this._template;
	}

	public boolean validate(Npc npc, List<Player> group, BiConsumer<Player, String> htmlCallback)
	{
		for (Player member : group)
		{
			if (!this.test(member, npc, group))
			{
				this.sendMessage(group, member, htmlCallback);
				return false;
			}

			if (this._leaderOnly)
			{
				break;
			}
		}

		return true;
	}

	private void sendMessage(List<Player> group, Player member, BiConsumer<Player, String> htmlCallback)
	{
		String html = this._parameters.getString("html", null);
		if (html != null && htmlCallback != null)
		{
			htmlCallback.accept(group.get(0), html);
			if (!this._showMessageAndHtml)
			{
				return;
			}
		}

		String message = this._parameters.getString("message", null);
		if (message != null)
		{
			if (this._leaderOnly)
			{
				member.sendMessage(message);
			}
			else
			{
				group.forEach(p -> p.sendMessage(message));
			}
		}
		else
		{
			if (this._systemMsg != null)
			{
				SystemMessage msg = new SystemMessage(this._systemMsg);
				if (this._systemMsgParams != null)
				{
					this._systemMsgParams.accept(msg, member);
				}

				if (this._leaderOnly)
				{
					member.sendPacket(msg);
				}
				else
				{
					group.forEach(p -> p.sendPacket(msg));
				}
			}
		}
	}

	public void applyEffect(List<Player> group)
	{
		for (Player member : group)
		{
			this.onSuccess(member);
			if (this._leaderOnly)
			{
				break;
			}
		}
	}

	protected void setSystemMessage(SystemMessageId msg)
	{
		this._systemMsg = msg;
	}

	protected void setSystemMessage(SystemMessageId msg, BiConsumer<SystemMessage, Player> params)
	{
		this.setSystemMessage(msg);
		this._systemMsgParams = params;
	}

	protected boolean test(Player player, Npc npc, List<Player> group)
	{
		return this.test(player, npc);
	}

	protected boolean test(Player player, Npc npc)
	{
		return true;
	}

	protected void onSuccess(Player player)
	{
	}
}
