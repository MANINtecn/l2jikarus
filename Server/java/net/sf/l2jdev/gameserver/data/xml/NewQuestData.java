package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.script.newquestdata.NewQuest;
import org.w3c.dom.Document;

public class NewQuestData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(NewQuestData.class.getName());
	private final Map<Integer, NewQuest> _newQuestData = new LinkedHashMap<>();

	protected NewQuestData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this._newQuestData.clear();
		this.parseDatapackFile("data/NewQuestData.xml");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._newQuestData.size() + " new quest data.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", listNode -> this.forEach(listNode, "quest", questNode -> {
			StatSet set = new StatSet(this.parseAttributes(questNode));
			this.forEach(questNode, "locations", locationsNode -> this.forEach(locationsNode, "param", paramNode -> set.set(this.parseString(paramNode.getAttributes(), "name"), paramNode.getTextContent())));
			this.forEach(questNode, "conditions", conditionsNode -> this.forEach(conditionsNode, "param", paramNode -> set.set(this.parseString(paramNode.getAttributes(), "name"), paramNode.getTextContent())));
			this.forEach(questNode, "rewards", rewardsNode -> {
				List<ItemHolder> rewardItems = new ArrayList<>();
				this.forEach(rewardsNode, "items", itemsNode -> this.forEach(itemsNode, "item", itemNode -> {
					int itemId = this.parseInteger(itemNode.getAttributes(), "id");
					int itemCount = this.parseInteger(itemNode.getAttributes(), "count");
					ItemHolder rewardItem = new ItemHolder(itemId, itemCount);
					rewardItems.add(rewardItem);
				}));
				set.set("rewardItems", rewardItems);
				this.forEach(rewardsNode, "param", paramNode -> set.set(this.parseString(paramNode.getAttributes(), "name"), paramNode.getTextContent()));
			});
			this.forEach(questNode, "goals", goalsNode -> this.forEach(goalsNode, "param", paramNode -> set.set(this.parseString(paramNode.getAttributes(), "name"), paramNode.getTextContent())));
			NewQuest holder = new NewQuest(set);
			this._newQuestData.put(holder.getId(), holder);
		}));
	}

	public NewQuest getQuestById(int id)
	{
		return this._newQuestData.get(id);
	}

	public Collection<NewQuest> getQuests()
	{
		return this._newQuestData.values();
	}

	public static NewQuestData getInstance()
	{
		return NewQuestData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final NewQuestData INSTANCE = new NewQuestData();
	}
}
