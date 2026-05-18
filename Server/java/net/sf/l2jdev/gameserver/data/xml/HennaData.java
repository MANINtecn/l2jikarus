package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.henna.Henna;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class HennaData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(HennaData.class.getName());
	private final Map<Integer, Henna> _hennaDyeIdList = new HashMap<>();
	private final Map<Integer, Henna> _hennaItemIdList = new HashMap<>();

	protected HennaData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this._hennaItemIdList.clear();
		this._hennaDyeIdList.clear();
		this.parseDatapackFile("data/stats/hennaList.xml");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._hennaDyeIdList.size() + " henna data.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		for (Node n = document.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equals(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("henna".equals(d.getNodeName()))
					{
						this.parseHenna(d);
					}
				}
			}
		}
	}

	private void parseHenna(Node d)
	{
		StatSet set = new StatSet();
		List<Integer> wearClassIds = new ArrayList<>();
		List<Skill> skills = new ArrayList<>();
		NamedNodeMap attrs = d.getAttributes();

		for (int i = 0; i < attrs.getLength(); i++)
		{
			Node attr = attrs.item(i);
			set.set(attr.getNodeName(), attr.getNodeValue());
		}

		for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
		{
			String name = c.getNodeName();
			attrs = c.getAttributes();
			switch (name)
			{
				case "stats":
					for (int i = 0; i < attrs.getLength(); i++)
					{
						Node attrx = attrs.item(i);
						set.set(attrx.getNodeName(), attrx.getNodeValue());
					}
					break;
				case "wear":
				{
					Node attr = attrs.getNamedItem("count");
					set.set("wear_count", attr.getNodeValue());
					attr = attrs.getNamedItem("fee");
					set.set("wear_fee", attr.getNodeValue());
					attr = attrs.getNamedItem("l2coinfee");
					if (attr != null)
					{
						set.set("l2coin_fee", attr.getNodeValue());
					}
					break;
				}
				case "cancel":
				{
					Node attr = attrs.getNamedItem("count");
					set.set("cancel_count", attr.getNodeValue());
					attr = attrs.getNamedItem("fee");
					set.set("cancel_fee", attr.getNodeValue());
					attr = attrs.getNamedItem("l2coinfee_cancel");
					if (attr != null)
					{
						set.set("cancel_l2coin_fee", attr.getNodeValue());
					}
					break;
				}
				case "duration":
				{
					Node attr = attrs.getNamedItem("time");
					set.set("duration", attr.getNodeValue());
					break;
				}
				case "skill":
					skills.add(SkillData.getInstance().getSkill(this.parseInteger(attrs, "id"), this.parseInteger(attrs, "level")));
					break;
				case "classId":
					for (String s : c.getTextContent().split(","))
					{
						wearClassIds.add(Integer.parseInt(s));
					}
			}
		}

		Henna henna = new Henna(set);
		henna.setSkills(skills);
		henna.setWearClassIds(wearClassIds);
		this._hennaDyeIdList.put(henna.getDyeId(), henna);
		this._hennaItemIdList.put(henna.getDyeItemId(), henna);
	}

	public Henna getHenna(int id)
	{
		return this._hennaDyeIdList.get(id);
	}

	public Henna getHennaByDyeId(int id)
	{
		return this._hennaDyeIdList.get(id);
	}

	public Henna getHennaByItemId(int id)
	{
		return this._hennaItemIdList.get(id);
	}

	public List<Henna> getHennaList(Player player)
	{
		List<Henna> list = new ArrayList<>();

		for (Henna henna : this._hennaDyeIdList.values())
		{
			if (henna.isAllowedClass(player))
			{
				list.add(henna);
			}
		}

		return list;
	}

	public static HennaData getInstance()
	{
		return HennaData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final HennaData INSTANCE = new HennaData();
	}
}
