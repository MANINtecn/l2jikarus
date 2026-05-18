package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.clan.ClanRewardBonus;
import net.sf.l2jdev.gameserver.model.clan.enums.ClanRewardType;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ClanRewardData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(ClanRewardData.class.getName());
	private final Map<ClanRewardType, List<ClanRewardBonus>> _clanRewards = new ConcurrentHashMap<>();

	protected ClanRewardData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this.parseDatapackFile("config/ClanReward.xml");

		for (ClanRewardType type : ClanRewardType.values())
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + (this._clanRewards.containsKey(type) ? this._clanRewards.get(type).size() : 0) + " rewards for " + type.toString().replace("_", " ").toLowerCase() + ".");
		}
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document.getFirstChild(), IXmlReader::isNode, listNode -> {
			String s0$ = listNode.getNodeName();
			switch (s0$)
			{
				case "membersOnline":
					this.parseMembersOnline(listNode);
					break;
				case "huntingBonus":
					this.parseHuntingBonus(listNode);
			}
		});
	}

	private void parseMembersOnline(Node node)
	{
		this.forEach(node, IXmlReader::isNode, memberNode -> {
			if ("players".equalsIgnoreCase(memberNode.getNodeName()))
			{
				NamedNodeMap attrs = memberNode.getAttributes();
				int requiredAmount = this.parseInteger(attrs, "size");
				int level = this.parseInteger(attrs, "level");
				ClanRewardBonus bonus = new ClanRewardBonus(ClanRewardType.MEMBERS_ONLINE, level, requiredAmount);
				this.forEach(memberNode, IXmlReader::isNode, skillNode -> {
					if ("skill".equalsIgnoreCase(skillNode.getNodeName()))
					{
						NamedNodeMap skillAttr = skillNode.getAttributes();
						int skillId = this.parseInteger(skillAttr, "id");
						int skillLevel = this.parseInteger(skillAttr, "level");
						bonus.setSkillReward(new SkillHolder(skillId, skillLevel));
					}
				});
				this._clanRewards.computeIfAbsent(bonus.getType(), _ -> new ArrayList<>()).add(bonus);
			}
		});
	}

	private void parseHuntingBonus(Node node)
	{
		this.forEach(node, IXmlReader::isNode, memberNode -> {
			if ("hunting".equalsIgnoreCase(memberNode.getNodeName()))
			{
				NamedNodeMap attrs = memberNode.getAttributes();
				int requiredAmount = this.parseInteger(attrs, "points");
				int level = this.parseInteger(attrs, "level");
				ClanRewardBonus bonus = new ClanRewardBonus(ClanRewardType.HUNTING_MONSTERS, level, requiredAmount);
				this.forEach(memberNode, IXmlReader::isNode, skillNode -> {
					if ("skill".equalsIgnoreCase(skillNode.getNodeName()))
					{
						NamedNodeMap skillAttr = skillNode.getAttributes();
						int skillId = this.parseInteger(skillAttr, "id");
						int skillLevel = this.parseInteger(skillAttr, "level");
						bonus.setSkillReward(new SkillHolder(skillId, skillLevel));
					}
				});
				this._clanRewards.computeIfAbsent(bonus.getType(), _ -> new ArrayList<>()).add(bonus);
			}
		});
	}

	public List<ClanRewardBonus> getClanRewardBonuses(ClanRewardType type)
	{
		return this._clanRewards.get(type);
	}

	public ClanRewardBonus getHighestReward(ClanRewardType type)
	{
		ClanRewardBonus selectedBonus = null;

		for (ClanRewardBonus currentBonus : this._clanRewards.get(type))
		{
			if (selectedBonus == null || selectedBonus.getLevel() < currentBonus.getLevel())
			{
				selectedBonus = currentBonus;
			}
		}

		return selectedBonus;
	}

	public Collection<List<ClanRewardBonus>> getClanRewardBonuses()
	{
		return this._clanRewards.values();
	}

	public static ClanRewardData getInstance()
	{
		return ClanRewardData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final ClanRewardData INSTANCE = new ClanRewardData();
	}
}
