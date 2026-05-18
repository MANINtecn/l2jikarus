package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.item.combination.CombinationItemType;
import net.sf.l2jdev.gameserver.model.item.henna.CombinationHenna;
import net.sf.l2jdev.gameserver.model.item.henna.CombinationHennaReward;
import org.w3c.dom.Document;

public class HennaCombinationData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(HennaCombinationData.class.getName());
	private final List<CombinationHenna> _henna = new ArrayList<>();

	protected HennaCombinationData()
	{
		this.load();
	}

	@Override
	public synchronized void load()
	{
		this._henna.clear();
		this.parseDatapackFile("data/stats/hennaCombinations.xml");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._henna.size() + " henna combinations.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", listNode -> this.forEach(listNode, "henna", hennaNode -> {
			CombinationHenna henna = new CombinationHenna(new StatSet(this.parseAttributes(hennaNode)));
			this.forEach(hennaNode, "reward", rewardNode -> {
				int hennaId = this.parseInteger(rewardNode.getAttributes(), "dyeId");
				int id = this.parseInteger(rewardNode.getAttributes(), "id", -1);
				int count = this.parseInteger(rewardNode.getAttributes(), "count", 0);
				CombinationItemType type = this.parseEnum(rewardNode.getAttributes(), CombinationItemType.class, "type");
				henna.addReward(new CombinationHennaReward(hennaId, id, count, type));
				if (id != -1 && ItemData.getInstance().getTemplate(id) == null)
				{
					LOGGER.info(this.getClass().getSimpleName() + ": Could not find item with id " + id);
				}

				if (hennaId != 0 && HennaData.getInstance().getHenna(hennaId) == null)
				{
					LOGGER.info(this.getClass().getSimpleName() + ": Could not find henna with id " + hennaId);
				}
			});
			this._henna.add(henna);
		}));
	}

	public List<CombinationHenna> getHenna()
	{
		return this._henna;
	}

	public CombinationHenna getByHenna(int hennaId)
	{
		for (CombinationHenna henna : this._henna)
		{
			if (henna.getHenna() == hennaId)
			{
				return henna;
			}
		}

		return null;
	}

	public static final HennaCombinationData getInstance()
	{
		return HennaCombinationData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final HennaCombinationData INSTANCE = new HennaCombinationData();
	}
}
