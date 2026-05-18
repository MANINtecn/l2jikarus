package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.data.holders.EquipmentUpgradeNormalHolder;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.holders.ItemEnchantHolder;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.network.serverpackets.equipmentupgradenormal.ExUpgradeSystemNormalResult;
import org.w3c.dom.Document;

public class EquipmentUpgradeNormalData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(EquipmentUpgradeNormalData.class.getName());
	private final Map<Integer, EquipmentUpgradeNormalHolder> _upgrades = new HashMap<>();
	private final Set<ItemHolder> _discount = new HashSet<>();
	private int _commission;

	protected EquipmentUpgradeNormalData()
	{
		this.load();
	}

	public void reload()
	{
		for (Player player : World.getInstance().getPlayers())
		{
			player.sendPacket(ExUpgradeSystemNormalResult.FAIL);
		}

		this.load();
	}

	@Override
	public void load()
	{
		this._commission = -1;
		this._discount.clear();
		this._upgrades.clear();
		this.parseDatapackFile("data/EquipmentUpgradeNormalData.xml");
		if (!this._upgrades.isEmpty())
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._upgrades.size() + " upgrade-normal equipment data. Adena commission is " + this._commission + ".");
		}

		if (!this._discount.isEmpty())
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._discount.size() + " upgrade-normal discount data.");
		}
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", listNode -> this.forEach(listNode, "params", paramNode -> this._commission = new StatSet(this.parseAttributes(paramNode)).getInt("commission")));
		if (this._commission < 0)
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Commission in file EquipmentUpgradeNormalData.xml not set or less than 0! Setting up default value - 100!");
			this._commission = 100;
		}

		this.forEach(document, "list", listNode -> this.forEach(listNode, "discount", discountNode -> this.forEach(discountNode, "item", itemNode -> {
			StatSet successSet = new StatSet(this.parseAttributes(itemNode));
			this._discount.add(new ItemHolder(successSet.getInt("id"), successSet.getLong("count")));
		})));
		this.forEach(document, "list", listNode -> this.forEach(listNode, "upgrade", upgradeNode -> {
			AtomicReference<ItemEnchantHolder> initialItem = new AtomicReference<>();
			AtomicReference<List<ItemEnchantHolder>> materialItems = new AtomicReference<>(new ArrayList<>());
			AtomicReference<List<ItemEnchantHolder>> onSuccessItems = new AtomicReference<>(new ArrayList<>());
			AtomicReference<List<ItemEnchantHolder>> onFailureItems = new AtomicReference<>(new ArrayList<>());
			AtomicReference<List<ItemEnchantHolder>> bonusItems = new AtomicReference<>(new ArrayList<>());
			AtomicReference<Double> bonusChance = new AtomicReference<>();
			StatSet headerSet = new StatSet(this.parseAttributes(upgradeNode));
			int id = headerSet.getInt("id");
			int type = headerSet.getInt("type");
			double chance = headerSet.getDouble("chance");
			long commission = this._commission == 0 ? 0L : headerSet.getLong("commission") / 100L * this._commission;
			this.forEach(upgradeNode, "upgradeItem", upgradeItemNode -> {
				StatSet initialSet = new StatSet(this.parseAttributes(upgradeItemNode));
				initialItem.set(new ItemEnchantHolder(initialSet.getInt("id"), initialSet.getLong("count"), initialSet.getByte("enchantLevel")));
				if (initialItem.get() == null)
				{
					LOGGER.warning(this.getClass().getSimpleName() + ": upgradeItem in file EquipmentUpgradeNormalData.xml for upgrade id " + id + " seems like broken!");
				}

				if (initialItem.get().getCount() < 0L)
				{
					LOGGER.warning(this.getClass().getSimpleName() + ": upgradeItem -> item -> count in file EquipmentUpgradeNormalData.xml for upgrade id " + id + " cannot be less than 0!");
				}
			});
			this.forEach(upgradeNode, "material", materialItemNode -> this.forEach(materialItemNode, "item", itemNode -> {
				StatSet successSet = new StatSet(this.parseAttributes(itemNode));
				materialItems.get().add(new ItemEnchantHolder(successSet.getInt("id"), successSet.getLong("count"), successSet.getInt("enchantLevel")));
			}));
			this.forEach(upgradeNode, "successItems", successItemNode -> this.forEach(successItemNode, "item", itemNode -> {
				StatSet successSet = new StatSet(this.parseAttributes(itemNode));
				onSuccessItems.get().add(new ItemEnchantHolder(successSet.getInt("id"), successSet.getLong("count"), successSet.getInt("enchantLevel")));
			}));
			this.forEach(upgradeNode, "failureItems", failureItemNode -> this.forEach(failureItemNode, "item", itemNode -> {
				StatSet successSet = new StatSet(this.parseAttributes(itemNode));
				onFailureItems.get().add(new ItemEnchantHolder(successSet.getInt("id"), successSet.getLong("count"), successSet.getInt("enchantLevel")));
			}));
			this.forEach(upgradeNode, "bonus_items", bonusItemNode -> {
				bonusChance.set(new StatSet(this.parseAttributes(bonusItemNode)).getDouble("chance"));
				if (bonusChance.get() < 0.0)
				{
					LOGGER.warning(this.getClass().getSimpleName() + ": bonus_items -> chance in file EquipmentUpgradeNormalData.xml for upgrade id " + id + " cannot be less than 0!");
				}

				this.forEach(bonusItemNode, "item", itemNode -> {
					StatSet successSet = new StatSet(this.parseAttributes(itemNode));
					bonusItems.get().add(new ItemEnchantHolder(successSet.getInt("id"), successSet.getLong("count"), successSet.getInt("enchantLevel")));
				});
			});
			this._upgrades.put(id, new EquipmentUpgradeNormalHolder(id, type, commission, chance, initialItem.get(), materialItems.get(), onSuccessItems.get(), onFailureItems.get(), bonusChance.get() == null ? 0.0 : bonusChance.get(), bonusItems.get()));
		}));
	}

	public EquipmentUpgradeNormalHolder getUpgrade(int id)
	{
		return this._upgrades.get(id);
	}

	public Set<ItemHolder> getDiscount()
	{
		return this._discount;
	}

	public int getCommission()
	{
		return this._commission;
	}

	public static EquipmentUpgradeNormalData getInstance()
	{
		return EquipmentUpgradeNormalData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final EquipmentUpgradeNormalData INSTANCE = new EquipmentUpgradeNormalData();
	}
}
