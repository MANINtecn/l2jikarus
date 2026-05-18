package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.ensoul.EnsoulFee;
import net.sf.l2jdev.gameserver.model.ensoul.EnsoulOption;
import net.sf.l2jdev.gameserver.model.ensoul.EnsoulStone;
import net.sf.l2jdev.gameserver.model.item.EtcItem;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class EnsoulData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(EnsoulData.class.getName());
	private final Map<Integer, EnsoulFee> _ensoulFees = new ConcurrentHashMap<>();
	private final Map<Integer, EnsoulOption> _ensoulOptions = new ConcurrentHashMap<>();
	private final Map<Integer, EnsoulStone> _ensoulStones = new ConcurrentHashMap<>();

	protected EnsoulData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this.parseDatapackDirectory("data/stats/ensoul", true);
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._ensoulFees.size() + " fees.");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._ensoulOptions.size() + " options.");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._ensoulStones.size() + " stones.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", listNode -> this.forEach(listNode, IXmlReader::isNode, ensoulNode -> {
			String s0$ = ensoulNode.getNodeName();
			switch (s0$)
			{
				case "fee":
					this.parseFees(ensoulNode);
					break;
				case "option":
					this.parseOptions(ensoulNode);
					break;
				case "stone":
					this.parseStones(ensoulNode);
			}
		}));
	}

	private void parseFees(Node ensoulNode)
	{
		Integer stoneId = this.parseInteger(ensoulNode.getAttributes(), "stoneId");
		EnsoulFee fee = new EnsoulFee(stoneId);
		this.forEach(ensoulNode, IXmlReader::isNode, feeNode -> {
			String s0$ = feeNode.getNodeName();
			switch (s0$)
			{
				case "first":
					this.parseFee(feeNode, fee, 0);
					break;
				case "secondary":
					this.parseFee(feeNode, fee, 1);
					break;
				case "third":
					this.parseFee(feeNode, fee, 2);
					break;
				case "reNormal":
					this.parseReFee(feeNode, fee, 0);
					break;
				case "reSecondary":
					this.parseReFee(feeNode, fee, 1);
					break;
				case "reThird":
					this.parseReFee(feeNode, fee, 2);
					break;
				case "remove":
					this.parseRemove(feeNode, fee);
			}
		});
	}

	private void parseFee(Node ensoulNode, EnsoulFee fee, int index)
	{
		NamedNodeMap attrs = ensoulNode.getAttributes();
		int id = this.parseInteger(attrs, "itemId");
		int count = this.parseInteger(attrs, "count");
		fee.setEnsoul(index, new ItemHolder(id, count));
		this._ensoulFees.put(fee.getStoneId(), fee);
	}

	private void parseReFee(Node ensoulNode, EnsoulFee fee, int index)
	{
		NamedNodeMap attrs = ensoulNode.getAttributes();
		int id = this.parseInteger(attrs, "itemId");
		int count = this.parseInteger(attrs, "count");
		fee.setResoul(index, new ItemHolder(id, count));
	}

	private void parseRemove(Node ensoulNode, EnsoulFee fee)
	{
		NamedNodeMap attrs = ensoulNode.getAttributes();
		int id = this.parseInteger(attrs, "itemId");
		int count = this.parseInteger(attrs, "count");
		fee.addRemovalFee(new ItemHolder(id, count));
	}

	private void parseOptions(Node ensoulNode)
	{
		NamedNodeMap attrs = ensoulNode.getAttributes();
		int id = this.parseInteger(attrs, "id");
		String name = this.parseString(attrs, "name");
		String desc = this.parseString(attrs, "desc");
		int skillId = this.parseInteger(attrs, "skillId");
		int skillLevel = this.parseInteger(attrs, "skillLevel");
		EnsoulOption option = new EnsoulOption(id, name, desc, skillId, skillLevel);
		this._ensoulOptions.put(option.getId(), option);
	}

	private void parseStones(Node ensoulNode)
	{
		NamedNodeMap attrs = ensoulNode.getAttributes();
		int id = this.parseInteger(attrs, "id");
		int slotType = this.parseInteger(attrs, "slotType");
		EnsoulStone stone = new EnsoulStone(id, slotType);
		this.forEach(ensoulNode, "option", optionNode -> stone.addOption(this.parseInteger(optionNode.getAttributes(), "id")));
		this._ensoulStones.put(stone.getId(), stone);
		((EtcItem) ItemData.getInstance().getTemplate(stone.getId())).setEnsoulStone();
	}

	public ItemHolder getEnsoulFee(int stoneId, int index)
	{
		EnsoulFee fee = this._ensoulFees.get(stoneId);
		return fee != null ? fee.getEnsoul(index) : null;
	}

	public ItemHolder getResoulFee(int stoneId, int index)
	{
		EnsoulFee fee = this._ensoulFees.get(stoneId);
		return fee != null ? fee.getResoul(index) : null;
	}

	public Collection<ItemHolder> getRemovalFee(int stoneId)
	{
		EnsoulFee fee = this._ensoulFees.get(stoneId);
		return fee != null ? fee.getRemovalFee() : Collections.emptyList();
	}

	public EnsoulOption getOption(int id)
	{
		return this._ensoulOptions.get(id);
	}

	public EnsoulStone getStone(int id)
	{
		return this._ensoulStones.get(id);
	}

	public int getStone(int type, int optionId)
	{
		for (EnsoulStone stone : this._ensoulStones.values())
		{
			if (stone.getSlotType() == type)
			{
				for (int id : stone.getOptions())
				{
					if (id == optionId)
					{
						return stone.getId();
					}
				}
			}
		}

		return 0;
	}

	public static EnsoulData getInstance()
	{
		return EnsoulData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final EnsoulData INSTANCE = new EnsoulData();
	}
}
