package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.item.holders.ElementalSpiritTemplateHolder;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ElementalSpiritData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(ElementalSpiritData.class.getName());
	public static final float FRAGMENT_XP_CONSUME = 50000.0F;
	public static final int TALENT_INIT_FEE = 50000;
	public static final int[] EXTRACT_FEES = new int[]
	{
		100000,
		200000,
		300000,
		600000,
		1500000
	};
	private static final Map<Byte, Map<Byte, ElementalSpiritTemplateHolder>> SPIRIT_DATA = new HashMap<>(4);

	protected ElementalSpiritData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this.parseDatapackFile("data/ElementalSpiritData.xml");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + SPIRIT_DATA.size() + " elemental spirit templates.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", list -> this.forEach(list, "spirit", this::parseSpirit));
	}

	private void parseSpirit(Node spiritNode)
	{
		NamedNodeMap attributes = spiritNode.getAttributes();
		byte type = this.parseByte(attributes, "type");
		byte stage = this.parseByte(attributes, "stage");
		int npcId = this.parseInteger(attributes, "npcId");
		int extractItem = this.parseInteger(attributes, "extractItem");
		int maxCharacteristics = this.parseInteger(attributes, "maxCharacteristics");
		ElementalSpiritTemplateHolder template = new ElementalSpiritTemplateHolder(type, stage, npcId, extractItem, maxCharacteristics);
		SPIRIT_DATA.computeIfAbsent(type, HashMap::new).put(stage, template);
		this.forEach(spiritNode, "level", levelNode -> {
			NamedNodeMap levelInfo = levelNode.getAttributes();
			int level = this.parseInteger(levelInfo, "id");
			int attack = this.parseInteger(levelInfo, "atk");
			int defense = this.parseInteger(levelInfo, "def");
			int criticalRate = this.parseInteger(levelInfo, "critRate");
			int criticalDamage = this.parseInteger(levelInfo, "critDam");
			long maxExperience = this.parseLong(levelInfo, "maxExp");
			template.addLevelInfo(level, attack, defense, criticalRate, criticalDamage, maxExperience);
		});
		this.forEach(spiritNode, "itemToEvolve", itemNode -> {
			NamedNodeMap itemInfo = itemNode.getAttributes();
			int itemId = this.parseInteger(itemInfo, "id");
			int count = this.parseInteger(itemInfo, "count", 1);
			template.addItemToEvolve(itemId, count);
		});
		this.forEach(spiritNode, "absorbItem", absorbItemNode -> {
			NamedNodeMap absorbInfo = absorbItemNode.getAttributes();
			int itemId = this.parseInteger(absorbInfo, "id");
			int experience = this.parseInteger(absorbInfo, "experience");
			template.addAbsorbItem(itemId, experience);
		});
	}

	public ElementalSpiritTemplateHolder getSpirit(byte type, byte stage)
	{
		return SPIRIT_DATA.containsKey(type) ? SPIRIT_DATA.get(type).get(stage) : null;
	}

	public static ElementalSpiritData getInstance()
	{
		return ElementalSpiritData.Singleton.INSTANCE;
	}

	private static class Singleton
	{
		static final ElementalSpiritData INSTANCE = new ElementalSpiritData();
	}
}
