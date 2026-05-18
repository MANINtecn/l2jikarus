package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.data.holders.EquipmentUpgradeHolder;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import org.w3c.dom.Document;

public class EquipmentUpgradeData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(EquipmentUpgradeData.class.getName());
	private static final Map<Integer, EquipmentUpgradeHolder> _upgrades = new HashMap<>();

	protected EquipmentUpgradeData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		_upgrades.clear();
		this.parseDatapackFile("data/EquipmentUpgradeData.xml");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + _upgrades.size() + " upgrade equipment data.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", listNode -> this.forEach(listNode, "upgrade", upgradeNode -> {
			StatSet set = new StatSet(this.parseAttributes(upgradeNode));
			int id = set.getInt("id");
			String[] item = set.getString("item").split(",");
			int requiredItemId = Integer.parseInt(item[0]);
			int requiredItemEnchant = Integer.parseInt(item[1]);
			String materials = set.getString("materials", "");
			List<ItemHolder> materialList = new ArrayList<>();
			if (!materials.isEmpty())
			{
				for (String mat : materials.split(";"))
				{
					String[] matValues = mat.split(",");
					int matItemId = Integer.parseInt(matValues[0]);
					long matItemCount = Long.parseLong(matValues[1]);
					if (ItemData.getInstance().getTemplate(matItemId) == null)
					{
						LOGGER.info(this.getClass().getSimpleName() + ": Material item with id " + matItemId + " does not exist.");
					}
					else
					{
						materialList.add(new ItemHolder(matItemId, matItemCount));
					}
				}
			}

			long adena = set.getLong("adena", 0L);
			String[] resultItem = set.getString("result").split(",");
			int resultItemId = Integer.parseInt(resultItem[0]);
			int resultItemEnchant = Integer.parseInt(resultItem[1]);
			boolean announce = set.getBoolean("announce", false);
			if (ItemData.getInstance().getTemplate(requiredItemId) == null)
			{
				LOGGER.info(this.getClass().getSimpleName() + ": Required item with id " + requiredItemId + " does not exist.");
			}
			else
			{
				EquipmentUpgradeHolder upgrade = new EquipmentUpgradeHolder(id, requiredItemId, requiredItemEnchant, materialList, adena, resultItemId, resultItemEnchant, announce);
				_upgrades.put(id, upgrade);
			}
		}));
	}

	public EquipmentUpgradeHolder getUpgrade(int id)
	{
		return _upgrades.get(id);
	}

	public static EquipmentUpgradeData getInstance()
	{
		return EquipmentUpgradeData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final EquipmentUpgradeData INSTANCE = new EquipmentUpgradeData();
	}
}
