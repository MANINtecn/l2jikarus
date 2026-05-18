package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.data.holders.MultisellEntryHolder;
import net.sf.l2jdev.gameserver.data.holders.MultisellListHolder;
import net.sf.l2jdev.gameserver.data.holders.PreparedMultisellListHolder;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.enchant.EnchantItemGroup;
import net.sf.l2jdev.gameserver.model.item.enums.BodyPart;
import net.sf.l2jdev.gameserver.model.item.enums.SpecialItemType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemChanceHolder;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.network.serverpackets.MultiSellList;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class MultisellData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(MultisellData.class.getName());
	public static final int PAGE_SIZE = 40;
	private final Map<Integer, MultisellListHolder> _multisells = new ConcurrentHashMap<>();

	protected MultisellData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this._multisells.clear();
		this.parseDatapackDirectory("data/multisell", false);
		this.parseDatapackDirectory("data/multisell/items", false);
		if (GeneralConfig.CUSTOM_MULTISELL_LOAD)
		{
			this.parseDatapackDirectory("data/multisell/custom", false);
		}

		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._multisells.size() + " multisell lists.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		EnchantItemGroup magicWeaponGroup = EnchantItemGroupsData.getInstance().getItemGroup("MAGE_WEAPON_GROUP");
		int magicWeaponGroupMax = magicWeaponGroup != null ? magicWeaponGroup.getMaximumEnchant() : -2;
		EnchantItemGroup weapongroup = EnchantItemGroupsData.getInstance().getItemGroup("FIGHTER_WEAPON_GROUP");
		int weaponGroupMax = weapongroup != null ? weapongroup.getMaximumEnchant() : -2;
		EnchantItemGroup fullArmorGroup = EnchantItemGroupsData.getInstance().getItemGroup("FULL_ARMOR_GROUP");
		int fullArmorGroupMax = fullArmorGroup != null ? fullArmorGroup.getMaximumEnchant() : -2;
		EnchantItemGroup armorGroup = EnchantItemGroupsData.getInstance().getItemGroup("ARMOR_GROUP");
		int armorGroupMax = armorGroup != null ? armorGroup.getMaximumEnchant() : -2;

		try
		{
			this.forEach(document, "list", listNode -> {
				StatSet set = new StatSet(this.parseAttributes(listNode));
				int listId = Integer.parseInt(file.getName().substring(0, file.getName().length() - 4));
				List<MultisellEntryHolder> entries = new ArrayList<>(listNode.getChildNodes().getLength());
				AtomicInteger entryCounter = new AtomicInteger();
				this.forEach(listNode, itemNode -> {
					if ("item".equalsIgnoreCase(itemNode.getNodeName()))
					{
						long totalPrice = 0L;
						int lastIngredientId = 0;
						long lastIngredientCount = 0L;
						entryCounter.incrementAndGet();
						List<ItemChanceHolder> ingredients = new ArrayList<>(1);
						List<ItemChanceHolder> products = new ArrayList<>(1);
						MultisellEntryHolder entry = new MultisellEntryHolder(ingredients, products);

						for (Node d = itemNode.getFirstChild(); d != null; d = d.getNextSibling())
						{
							if ("ingredient".equalsIgnoreCase(d.getNodeName()))
							{
								int id = this.parseInteger(d.getAttributes(), "id");
								long count = this.parseLong(d.getAttributes(), "count");
								byte enchantmentLevel = this.parseByte(d.getAttributes(), "enchantmentLevel", (byte) 0);
								Boolean maintainIngredient = this.parseBoolean(d.getAttributes(), "maintainIngredient", false);
								ItemChanceHolder ingredient = new ItemChanceHolder(id, 0.0, count, enchantmentLevel, maintainIngredient);
								if (itemExists(ingredient))
								{
									ingredients.add(ingredient);
									lastIngredientId = id;
									lastIngredientCount = count;
								}
								else
								{
									LOGGER.warning("Invalid ingredient id or count for itemId: " + ingredient.getId() + ", count: " + ingredient.getCount() + " in list: " + listId);
								}
							}
							else if ("production".equalsIgnoreCase(d.getNodeName()))
							{
								int id = this.parseInteger(d.getAttributes(), "id");
								long count = this.parseLong(d.getAttributes(), "count");
								double chance = this.parseDouble(d.getAttributes(), "chance", Double.NaN);
								byte enchantmentLevel = this.parseByte(d.getAttributes(), "enchantmentLevel", (byte) 0);
								if (enchantmentLevel > 0)
								{
									ItemTemplate item = ItemData.getInstance().getTemplate(id);
									if (item != null)
									{
										if (item.isWeapon())
										{
											enchantmentLevel = (byte) Math.min(enchantmentLevel, item.isMagicWeapon() ? (magicWeaponGroupMax > -2 ? magicWeaponGroupMax : enchantmentLevel) : (weaponGroupMax > -2 ? weaponGroupMax : enchantmentLevel));
										}
										else if (item.isArmor())
										{
											enchantmentLevel = (byte) Math.min(enchantmentLevel, item.getBodyPart() == BodyPart.FULL_ARMOR ? (fullArmorGroupMax > -2 ? fullArmorGroupMax : enchantmentLevel) : (armorGroupMax > -2 ? armorGroupMax : enchantmentLevel));
										}
									}
								}

								ItemChanceHolder product = new ItemChanceHolder(id, chance, count, enchantmentLevel);
								if (itemExists(product))
								{
									if ((Double.isNaN(chance) || !(chance < 0.0)) && !(chance > 100.0))
									{
										products.add(product);
										ItemTemplate item = ItemData.getInstance().getTemplate(id);
										if (item != null)
										{
											if (chance > 0.0)
											{
												totalPrice = (long) (totalPrice + item.getReferencePrice() / 2 * count * (chance / 100.0));
											}
											else
											{
												totalPrice += item.getReferencePrice() / 2 * count;
											}
										}
									}
									else
									{
										LOGGER.warning("Invalid chance for itemId: " + product.getId() + ", count: " + product.getCount() + ", chance: " + chance + " in list: " + listId);
									}
								}
								else
								{
									LOGGER.warning("Invalid product id or count for itemId: " + product.getId() + ", count: " + product.getCount() + " in list: " + listId);
								}
							}
						}

						double totalChance = products.stream().filter(i -> !Double.isNaN(i.getChance())).mapToDouble(ItemChanceHolder::getChance).sum();
						if (totalChance > 100.0)
						{
							LOGGER.warning("Products' total chance of " + totalChance + "% exceeds 100% for list: " + listId + " at entry " + entries.size() + "1.");
						}

						if (GeneralConfig.CORRECT_PRICES && ingredients.size() == 1 && lastIngredientId == 57 && lastIngredientCount < totalPrice)
						{
							LOGGER.warning("Buy price " + lastIngredientCount + " is less than sell price " + totalPrice + " at entry " + entryCounter.intValue() + " of multisell " + listId + ".");
							ItemChanceHolder ingredient = new ItemChanceHolder(57, 0.0, totalPrice, (byte) 0, ingredients.get(0).isMaintainIngredient());
							ingredients.clear();
							ingredients.add(ingredient);
						}

						entries.add(entry);
					}
					else if ("npcs".equalsIgnoreCase(itemNode.getNodeName()))
					{
						Set<Integer> allowNpc = new HashSet<>(itemNode.getChildNodes().getLength());
						this.forEach(itemNode, n -> "npc".equalsIgnoreCase(n.getNodeName()), n -> allowNpc.add(Integer.parseInt(n.getTextContent())));
						set.set("allowNpc", allowNpc);
					}
				});
				set.set("listId", listId);
				set.set("entries", entries);
				if (this._multisells.put(listId, new MultisellListHolder(set)) != null)
				{
					LOGGER.log(Level.SEVERE, this.getClass().getSimpleName() + ": Duplicate multisell with id: " + listId);
				}
			});
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.SEVERE, this.getClass().getSimpleName() + ": Error in file " + file, var12);
		}
	}

	@Override
	public boolean isValidXmlFile(File file)
	{
		return file != null && file.isFile() && file.getName().toLowerCase().matches("\\d+\\.xml");
	}

	public void separateAndSend(int listId, Player player, Npc npc, boolean inventoryOnly, double ingredientMultiplierValue, double productMultiplierValue, int type)
	{
		MultisellListHolder template = this._multisells.get(listId);
		if (template == null)
		{
			LOGGER.warning("Can't find list id: " + listId + " requested by player: " + player.getName() + ", npcId: " + (npc != null ? npc.getId() : 0));
		}
		else
		{
			if (!template.isNpcAllowed(-1) && (npc == null || !template.isNpcAllowed(npc.getId())))
			{
				if (!player.isGM())
				{
					LOGGER.warning(this.getClass().getSimpleName() + ": " + player + " attempted to open multisell " + listId + " from npc " + npc + " which is not allowed!");
					return;
				}

				player.sendMessage("Multisell " + listId + " is restricted. Under current conditions cannot be used. Only GMs are allowed to use it.");
			}

			double ingredientMultiplier = Double.isNaN(ingredientMultiplierValue) ? template.getIngredientMultiplier() : ingredientMultiplierValue;
			double productMultiplier = Double.isNaN(productMultiplierValue) ? template.getProductMultiplier() : productMultiplierValue;
			PreparedMultisellListHolder list = new PreparedMultisellListHolder(template, inventoryOnly, player.getInventory(), npc, ingredientMultiplier, productMultiplier);
			int index = 0;

			do
			{
				player.sendPacket(new MultiSellList(player, list, index, type));
				index += 40;
			}
			while (index < list.getEntries().size());

			player.setMultiSell(list);
		}
	}

	public void separateAndSend(int listId, Player player, Npc npc, boolean inventoryOnly)
	{
		this.separateAndSend(listId, player, npc, inventoryOnly, Double.NaN, Double.NaN, 0);
	}

	private final static boolean itemExists(ItemHolder holder)
	{
		SpecialItemType specialItem = SpecialItemType.getByClientId(holder.getId());
		if (specialItem != null)
		{
			return true;
		}
		ItemTemplate template = ItemData.getInstance().getTemplate(holder.getId());
		return template != null && (template.isStackable() ? holder.getCount() >= 1L : holder.getCount() == 1L);
	}

	public MultisellListHolder getMultisell(int id)
	{
		return this._multisells.getOrDefault(id, null);
	}

	public static MultisellData getInstance()
	{
		return MultisellData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final MultisellData INSTANCE = new MultisellData();
	}
}
