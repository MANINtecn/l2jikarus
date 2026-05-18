package net.sf.l2jdev.gameserver.model.script;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.sf.l2jdev.gameserver.model.KeyValuePair;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;

public class QuestCondition
{
	private final Predicate<Player> _condition;
	private Map<Integer, String> _perNpcDialog;
	private final String _html;

	public QuestCondition(Predicate<Player> cond, String html)
	{
		this._condition = cond;
		this._html = html;
	}

	@SafeVarargs
	public QuestCondition(Predicate<Player> cond, KeyValuePair<Integer, String>... pairs)
	{
		this._condition = cond;
		this._html = null;
		this._perNpcDialog = new HashMap<>();
		Stream.of(pairs).forEach(pair -> this._perNpcDialog.put(pair.getKey(), pair.getValue()));
	}

	public boolean test(Player player)
	{
		return this._condition.test(player);
	}

	public String getHtml(Npc npc)
	{
		return this._perNpcDialog != null ? this._perNpcDialog.get(npc.getId()) : this._html;
	}
}
