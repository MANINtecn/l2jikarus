package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PlayerClass;
import net.sf.l2jdev.gameserver.model.actor.holders.player.DailyMissionDataHolder;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import org.w3c.dom.Document;

public class DailyMissionData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(DailyMissionData.class.getName());
	private final Map<Integer, List<DailyMissionDataHolder>> _dailyMissionRewards = new LinkedHashMap<>();
	private final List<DailyMissionDataHolder> _dailyMissionData = new ArrayList<>();
	private boolean _isAvailable;

	protected DailyMissionData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this._dailyMissionRewards.clear();
		this.parseDatapackFile("data/DailyMission.xml");
		this._dailyMissionData.clear();

		for (List<DailyMissionDataHolder> missionList : this._dailyMissionRewards.values())
		{
			this._dailyMissionData.addAll(missionList);
		}

		this._isAvailable = !this._dailyMissionRewards.isEmpty();
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._dailyMissionRewards.size() + " one day rewards.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", listNode -> this.forEach(listNode, "reward", rewardNode -> {
			StatSet set = new StatSet(this.parseAttributes(rewardNode));
			List<ItemHolder> items = new ArrayList<>(1);
			this.forEach(rewardNode, "items", itemsNode -> this.forEach(itemsNode, "item", itemNode -> {
				int itemId = this.parseInteger(itemNode.getAttributes(), "id");
				int itemCount = this.parseInteger(itemNode.getAttributes(), "count");
				if (itemId != 97224 || MissionLevel.getInstance().getCurrentSeason() > 0)
				{
					items.add(new ItemHolder(itemId, itemCount));
				}
			}));
			set.set("items", items);
			List<PlayerClass> classRestriction = new ArrayList<>(1);
			this.forEach(rewardNode, "classId", classRestrictionNode -> classRestriction.add(PlayerClass.getPlayerClass(Integer.parseInt(classRestrictionNode.getTextContent()))));
			set.set("classRestriction", classRestriction);
			set.set("handler", "");
			set.set("params", StatSet.EMPTY_STATSET);
			this.forEach(rewardNode, "handler", handlerNode -> {
				set.set("handler", this.parseString(handlerNode.getAttributes(), "name"));
				StatSet params = new StatSet();
				set.set("params", params);
				this.forEach(handlerNode, "param", paramNode -> params.set(this.parseString(paramNode.getAttributes(), "name"), paramNode.getTextContent()));
			});
			DailyMissionDataHolder holder = new DailyMissionDataHolder(set);
			this._dailyMissionRewards.computeIfAbsent(holder.getId(), _ -> new ArrayList<>()).add(holder);
		}));
	}

	public Collection<DailyMissionDataHolder> getDailyMissionData()
	{
		return this._dailyMissionData;
	}

	public Collection<DailyMissionDataHolder> getDailyMissionData(Player player)
	{
		List<DailyMissionDataHolder> missionData = new LinkedList<>();

		for (DailyMissionDataHolder mission : this._dailyMissionData)
		{
			if (mission.isDisplayable(player))
			{
				missionData.add(mission);
			}
		}

		return missionData;
	}

	public Collection<DailyMissionDataHolder> getDailyMissionData(int id)
	{
		return this._dailyMissionRewards.get(id);
	}

	public boolean isAvailable()
	{
		return this._isAvailable;
	}

	public static DailyMissionData getInstance()
	{
		return DailyMissionData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final DailyMissionData INSTANCE = new DailyMissionData();
	}
}
