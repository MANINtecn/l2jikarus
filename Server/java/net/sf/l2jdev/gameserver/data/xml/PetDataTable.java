package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.data.enums.EvolveLevel;
import net.sf.l2jdev.gameserver.model.PetData;
import net.sf.l2jdev.gameserver.model.PetLevelData;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.enums.player.MountType;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class PetDataTable implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(PetDataTable.class.getName());
	private final Map<Integer, PetData> _pets = new ConcurrentHashMap<>();
	private final Map<Integer, String> _petNames = new ConcurrentHashMap<>();

	protected PetDataTable()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this._pets.clear();
		this.parseDatapackDirectory("data/stats/pets", false);

		try (Connection conn = DatabaseFactory.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT * FROM pets"); ResultSet rs = ps.executeQuery();)
		{
			while (rs.next())
			{
				String name = rs.getString("name");
				if (name == null)
				{
					name = "No name";
				}

				this._petNames.put(rs.getInt("item_obj_id"), name);
			}
		}
		catch (Exception var12)
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Problem loading pet names! " + var12);
		}

		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._pets.size() + " pets.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		Node n = document.getFirstChild();

		for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
		{
			if (d.getNodeName().equals("pet"))
			{
				int npcId = this.parseInteger(d.getAttributes(), "id");
				int itemId = this.parseInteger(d.getAttributes(), "itemId");
				Integer index = this.parseInteger(d.getAttributes(), "index");
				Integer defaultPetType = this.parseInteger(d.getAttributes(), "defaultPetType");
				EvolveLevel evolveLevel = this.parseEnum(d.getAttributes(), EvolveLevel.class, "evolveLevel");
				Integer petType = this.parseInteger(d.getAttributes(), "type");
				if (defaultPetType == null)
				{
					defaultPetType = 0;
				}

				if (index == null)
				{
					index = 0;
				}

				if (petType == null)
				{
					petType = 0;
				}

				PetData data = new PetData(npcId, itemId, defaultPetType, evolveLevel, index, petType);

				for (Node p = d.getFirstChild(); p != null; p = p.getNextSibling())
				{
					if (p.getNodeName().equals("set"))
					{
						NamedNodeMap attrs = p.getAttributes();
						String type = attrs.getNamedItem("name").getNodeValue();
						if ("food".equals(type))
						{
							for (String foodId : attrs.getNamedItem("val").getNodeValue().split(";"))
							{
								data.addFood(Integer.parseInt(foodId));
							}
						}
						else if ("load".equals(type))
						{
							data.setLoad(this.parseInteger(attrs, "val"));
						}
						else if ("hungry_limit".equals(type))
						{
							data.setHungryLimit(this.parseInteger(attrs, "val"));
						}
						else if ("sync_level".equals(type))
						{
							data.setSyncLevel(this.parseInteger(attrs, "val") == 1);
						}
					}
					else if (p.getNodeName().equals("skills"))
					{
						for (Node s = p.getFirstChild(); s != null; s = s.getNextSibling())
						{
							if (s.getNodeName().equals("skill"))
							{
								NamedNodeMap attrs = s.getAttributes();
								data.addNewSkill(this.parseInteger(attrs, "skillId"), this.parseInteger(attrs, "skillLevel"), this.parseInteger(attrs, "minLevel"));
							}
						}
					}
					else if (p.getNodeName().equals("stats"))
					{
						for (Node sx = p.getFirstChild(); sx != null; sx = sx.getNextSibling())
						{
							if (sx.getNodeName().equals("stat"))
							{
								int level = Integer.parseInt(sx.getAttributes().getNamedItem("level").getNodeValue());
								StatSet set = new StatSet();

								for (Node bean = sx.getFirstChild(); bean != null; bean = bean.getNextSibling())
								{
									if (bean.getNodeName().equals("set"))
									{
										NamedNodeMap attrs = bean.getAttributes();
										if (attrs.getNamedItem("name").getNodeValue().equals("speed_on_ride"))
										{
											set.set("walkSpeedOnRide", attrs.getNamedItem("walk").getNodeValue());
											set.set("runSpeedOnRide", attrs.getNamedItem("run").getNodeValue());
											set.set("slowSwimSpeedOnRide", attrs.getNamedItem("slowSwim").getNodeValue());
											set.set("fastSwimSpeedOnRide", attrs.getNamedItem("fastSwim").getNodeValue());
											if (attrs.getNamedItem("slowFly") != null)
											{
												set.set("slowFlySpeedOnRide", attrs.getNamedItem("slowFly").getNodeValue());
											}

											if (attrs.getNamedItem("fastFly") != null)
											{
												set.set("fastFlySpeedOnRide", attrs.getNamedItem("fastFly").getNodeValue());
											}
										}
										else
										{
											set.set(attrs.getNamedItem("name").getNodeValue(), attrs.getNamedItem("val").getNodeValue());
										}
									}
								}

								data.addNewStat(level, new PetLevelData(set));
							}
						}
					}
				}

				this._pets.put(npcId, data);
			}
		}
	}

	public PetData getPetDataByItemId(int itemId)
	{
		for (PetData data : this._pets.values())
		{
			if (data.getItemId() == itemId)
			{
				return data;
			}
		}

		return null;
	}

	public PetLevelData getPetLevelData(int petId, int petLevel)
	{
		PetData pd = this.getPetData(petId);
		if (pd != null)
		{
			return petLevel > pd.getMaxLevel() ? pd.getPetLevelData(pd.getMaxLevel()) : pd.getPetLevelData(petLevel);
		}
		return null;
	}

	public PetData getPetData(int petId)
	{
		if (!this._pets.containsKey(petId))
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Missing pet data for npcid: " + petId);
		}

		return this._pets.get(petId);
	}

	public int getPetMinLevel(int petId)
	{
		return this._pets.get(petId).getMinLevel();
	}

	public int getPetItemsByNpc(int npcId)
	{
		return this._pets.get(npcId).getItemId();
	}

	public static boolean isMountable(int npcId)
	{
		return MountType.findByNpcId(npcId) != MountType.NONE;
	}

	public int getTypeByIndex(int index)
	{
		Entry<Integer, PetData> first = this._pets.entrySet().stream().filter(it -> it.getValue().getIndex() == index).findFirst().orElse(null);
		return first == null ? 0 : first.getValue().getType();
	}

	public PetData getPetDataByEvolve(int itemId, EvolveLevel evolveLevel, int index)
	{
		Optional<Entry<Integer, PetData>> firstByItem = this._pets.entrySet().stream().filter(it -> it.getValue().getItemId() == itemId && it.getValue().getIndex() == index && it.getValue().getEvolveLevel() == evolveLevel).findFirst();
		return firstByItem.map(Entry::getValue).orElse(null);
	}

	public PetData getPetDataByEvolve(int itemId, EvolveLevel evolveLevel)
	{
		Optional<Entry<Integer, PetData>> firstByItem = this._pets.entrySet().stream().filter(it -> it.getValue().getItemId() == itemId && it.getValue().getEvolveLevel() == evolveLevel).findFirst();
		return firstByItem.map(Entry::getValue).orElse(null);
	}

	public List<PetData> getPetDatasByEvolve(int itemId, EvolveLevel evolveLevel)
	{
		return this._pets.values().stream().filter(petData -> petData.getItemId() == itemId && petData.getEvolveLevel() == evolveLevel).collect(Collectors.toList());
	}

	public void setPetName(int objectId, String name)
	{
		this._petNames.put(objectId, name);
	}

	public String getPetName(int objectId)
	{
		return this._petNames.getOrDefault(objectId, "No name");
	}

	public String getNameByItemObjectId(int objectId)
	{
		String name = this.getPetName(objectId);
		SkillHolder type = PetTypeData.getInstance().getSkillByName(name);
		return type == null ? "" : type.getSkillId() + ";" + type.getSkillLevel() + ";" + PetTypeData.getInstance().getIdByName(name);
	}

	public static PetDataTable getInstance()
	{
		return PetDataTable.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final PetDataTable INSTANCE = new PetDataTable();
	}
}
