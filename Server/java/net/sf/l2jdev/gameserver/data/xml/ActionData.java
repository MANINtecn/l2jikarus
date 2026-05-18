package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.ActionDataHolder;
import net.sf.l2jdev.gameserver.model.StatSet;
import org.w3c.dom.Document;

public class ActionData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(ActionData.class.getName());
	private final Map<Integer, ActionDataHolder> _actionData = new HashMap<>();
	private final Map<Integer, Integer> _actionSkillsData = new HashMap<>();

	protected ActionData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this._actionData.clear();
		this._actionSkillsData.clear();
		this.parseDatapackFile("data/ActionData.xml");

		for (ActionDataHolder holder : this._actionData.values())
		{
			if (holder.getHandler().equals("PetSkillUse") || holder.getHandler().equals("ServitorSkillUse"))
			{
				this._actionSkillsData.put(holder.getOptionId(), holder.getId());
			}
		}

		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._actionData.size() + " player actions.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", listNode -> this.forEach(listNode, "action", actionNode -> {
			ActionDataHolder holder = new ActionDataHolder(new StatSet(this.parseAttributes(actionNode)));
			this._actionData.put(holder.getId(), holder);
		}));
	}

	public ActionDataHolder getActionData(int id)
	{
		return this._actionData.get(id);
	}

	public int getSkillActionId(int skillId)
	{
		return this._actionSkillsData.getOrDefault(skillId, -1);
	}

	public int[] getActionIdList()
	{
		return this._actionData.keySet().stream().mapToInt(Number::intValue).toArray();
	}

	public static ActionData getInstance()
	{
		return ActionData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final ActionData INSTANCE = new ActionData();
	}
}
