package net.sf.l2jdev.gameserver.data.xml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.config.ThreadConfig;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.config.DevelopmentConfig;
import net.sf.l2jdev.gameserver.config.ServerConfig;
import net.sf.l2jdev.gameserver.config.custom.FakePlayersConfig;
import net.sf.l2jdev.gameserver.data.SpawnTable;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.Spawn;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.holders.npc.ChanceLocation;
import net.sf.l2jdev.gameserver.model.actor.holders.npc.MinionHolder;
import net.sf.l2jdev.gameserver.model.actor.templates.NpcTemplate;
import net.sf.l2jdev.gameserver.model.interfaces.IParameterized;
import net.sf.l2jdev.gameserver.model.interfaces.ITerritorized;
import net.sf.l2jdev.gameserver.model.skill.holders.SkillHolder;
import net.sf.l2jdev.gameserver.model.spawns.NpcSpawnTemplate;
import net.sf.l2jdev.gameserver.model.spawns.SpawnGroup;
import net.sf.l2jdev.gameserver.model.spawns.SpawnTemplate;
import net.sf.l2jdev.gameserver.model.zone.ZoneForm;
import net.sf.l2jdev.gameserver.model.zone.form.ZoneCuboid;
import net.sf.l2jdev.gameserver.model.zone.form.ZoneCylinder;
import net.sf.l2jdev.gameserver.model.zone.form.ZoneNPoly;
import net.sf.l2jdev.gameserver.model.zone.type.BannedSpawnTerritory;
import net.sf.l2jdev.gameserver.model.zone.type.SpawnTerritory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class SpawnData implements IXmlReader
{
	protected static final Logger LOGGER = Logger.getLogger(SpawnData.class.getName());
	public static final String OTHER_XML_FOLDER = "data/spawns/Others";
	private final Collection<SpawnTemplate> _spawnTemplates = ConcurrentHashMap.newKeySet();

	protected SpawnData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this.parseDatapackDirectory("data/spawns", true);
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._spawnTemplates.stream().flatMap(c -> c.getGroups().stream()).flatMap(c -> c.getSpawns().stream()).count() + " spawns");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", listNode -> this.forEach(listNode, "spawn", spawnNode -> {
			try
			{
				this.parseSpawn(spawnNode, file, this._spawnTemplates);
			}
			catch (Exception var4)
			{
				LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Error while processing spawn in file: " + file.getAbsolutePath(), var4);
			}
		}));
	}

	public void parseSpawn(Node spawnsNode, File file, Collection<SpawnTemplate> spawns)
	{
		SpawnTemplate spawnTemplate = new SpawnTemplate(new StatSet(this.parseAttributes(spawnsNode)), file);
		SpawnGroup defaultGroup = null;

		for (Node innerNode = spawnsNode.getFirstChild(); innerNode != null; innerNode = innerNode.getNextSibling())
		{
			String var7 = innerNode.getNodeName();
			switch (var7)
			{
				case "territories":
					this.parseTerritories(innerNode, spawnTemplate.getFile(), spawnTemplate);
					break;
				case "group":
					this.parseGroup(innerNode, spawnTemplate);
					break;
				case "npc":
					if (defaultGroup == null)
					{
						defaultGroup = new SpawnGroup(StatSet.EMPTY_STATSET);
					}

					this.parseNpc(innerNode, spawnTemplate, defaultGroup);
					break;
				case "parameters":
					this.parseParameters(innerNode, spawnTemplate);
			}
		}

		if (defaultGroup != null)
		{
			spawnTemplate.addGroup(defaultGroup);
		}

		spawns.add(spawnTemplate);
	}

	private void parseTerritories(Node innerNode, File file, ITerritorized spawnTemplate)
	{
		this.forEach(innerNode, IXmlReader::isNode, territoryNode -> {
			String name = this.parseString(territoryNode.getAttributes(), "name", file.getName() + "_" + (spawnTemplate.getTerritories().size() + 1));
			int minZ = this.parseInteger(territoryNode.getAttributes(), "minZ");
			int maxZ = this.parseInteger(territoryNode.getAttributes(), "maxZ");
			List<Integer> xNodes = new ArrayList<>();
			List<Integer> yNodes = new ArrayList<>();
			this.forEach(territoryNode, "node", node -> {
				xNodes.add(this.parseInteger(node.getAttributes(), "x"));
				yNodes.add(this.parseInteger(node.getAttributes(), "y"));
			});
			int[] x = xNodes.stream().mapToInt(Integer::valueOf).toArray();
			int[] y = yNodes.stream().mapToInt(Integer::valueOf).toArray();
			ZoneForm zoneForm = null;
			String zoneShape = this.parseString(territoryNode.getAttributes(), "shape", "NPoly");
			switch (zoneShape)
			{
				case "Cuboid":
					zoneForm = new ZoneCuboid(x[0], x[1], y[0], y[1], minZ, maxZ);
					break;
				case "NPoly":
					zoneForm = new ZoneNPoly(x, y, minZ, maxZ);
					break;
				case "Cylinder":
					int zoneRad = Integer.parseInt(territoryNode.getAttributes().getNamedItem("rad").getNodeValue());
					zoneForm = new ZoneCylinder(x[0], y[0], minZ, maxZ, zoneRad);
			}

			String s2$ = territoryNode.getNodeName();
			switch (s2$)
			{
				case "territory":
					spawnTemplate.addTerritory(new SpawnTerritory(name, zoneForm));
					break;
				case "banned_territory":
					spawnTemplate.addBannedTerritory(new BannedSpawnTerritory(name, zoneForm));
			}
		});
	}

	private void parseGroup(Node n, SpawnTemplate spawnTemplate)
	{
		SpawnGroup group = new SpawnGroup(new StatSet(this.parseAttributes(n)));
		this.forEach(n, IXmlReader::isNode, npcNode -> {
			String s0$ = npcNode.getNodeName();
			switch (s0$)
			{
				case "territories":
					this.parseTerritories(npcNode, spawnTemplate.getFile(), group);
					break;
				case "npc":
					this.parseNpc(npcNode, spawnTemplate, group);
			}
		});
		spawnTemplate.addGroup(group);
	}

	private void parseNpc(Node n, SpawnTemplate spawnTemplate, SpawnGroup group)
	{
		NpcSpawnTemplate npcTemplate = new NpcSpawnTemplate(spawnTemplate, group, new StatSet(this.parseAttributes(n)));
		int npcId = npcTemplate.getId();
		NpcTemplate template = NpcData.getInstance().getTemplate(npcId);
		if (template != null)
		{
			if (!template.isType("Servitor") && !template.isType("Pet"))
			{
				if (FakePlayersConfig.FAKE_PLAYERS_ENABLED || !template.isFakePlayer())
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("parameters".equalsIgnoreCase(d.getNodeName()))
						{
							this.parseParameters(d, npcTemplate);
						}
						else if ("minions".equalsIgnoreCase(d.getNodeName()))
						{
							this.parseMinions(d, npcTemplate);
						}
						else if ("locations".equalsIgnoreCase(d.getNodeName()))
						{
							this.parseLocations(d, npcTemplate);
						}
					}

					group.addSpawn(npcTemplate);
				}
			}
			else
			{
				LOGGER.warning(this.getClass().getSimpleName() + ": Requested spawn for " + template.getType() + " " + template.getName() + "(" + template.getId() + ") in file: " + spawnTemplate.getFile().getName());
			}
		}
		else
		{
			if (FakePlayersConfig.FAKE_PLAYERS_ENABLED || npcId < 80000 && npcId > 89999)
			{
				LOGGER.warning(this.getClass().getSimpleName() + ": Requested spawn for non-existing NPC: " + npcId + " in file: " + spawnTemplate.getFile().getName());
			}
		}
	}

	private void parseParameters(Node node, IParameterized<StatSet> npcTemplate)
	{
		Map<String, Object> parameters = new HashMap<>();

		for (Node parameterNode = node.getFirstChild(); parameterNode != null; parameterNode = parameterNode.getNextSibling())
		{
			NamedNodeMap attributes = parameterNode.getAttributes();
			String var6 = parameterNode.getNodeName().toLowerCase();
			switch (var6)
			{
				case "param":
					parameters.put(this.parseString(attributes, "name"), this.parseString(attributes, "value"));
					break;
				case "skill":
					parameters.put(this.parseString(attributes, "name"), new SkillHolder(this.parseInteger(attributes, "id"), this.parseInteger(attributes, "level")));
					break;
				case "location":
					parameters.put(this.parseString(attributes, "name"), new Location(this.parseInteger(attributes, "x"), this.parseInteger(attributes, "y"), this.parseInteger(attributes, "z"), this.parseInteger(attributes, "heading", 0)));
					break;
				case "minions":
					List<MinionHolder> minions = new ArrayList<>(1);
					Node minionNode = parameterNode.getFirstChild();

					for (; minionNode != null; minionNode = minionNode.getNextSibling())
					{
						if (minionNode.getNodeName().equalsIgnoreCase("npc"))
						{
							attributes = minionNode.getAttributes();
							minions.add(new MinionHolder(this.parseInteger(attributes, "id"), this.parseInteger(attributes, "count"), this.parseInteger(attributes, "max", 0), this.parseInteger(attributes, "respawnTime").intValue(), this.parseInteger(attributes, "weightPoint", 0)));
						}
					}

					if (!minions.isEmpty())
					{
						parameters.put(this.parseString(parameterNode.getAttributes(), "name"), minions);
					}
			}
		}

		npcTemplate.setParameters(!parameters.isEmpty() ? new StatSet(Collections.unmodifiableMap(parameters)) : StatSet.EMPTY_STATSET);
	}

	private void parseMinions(Node n, NpcSpawnTemplate npcTemplate)
	{
		this.forEach(n, "minion", minionNode -> npcTemplate.addMinion(new MinionHolder(new StatSet(this.parseAttributes(minionNode)))));
	}

	private void parseLocations(Node n, NpcSpawnTemplate npcTemplate)
	{
		for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
		{
			if ("location".equalsIgnoreCase(d.getNodeName()))
			{
				int x = this.parseInteger(d.getAttributes(), "x");
				int y = this.parseInteger(d.getAttributes(), "y");
				int z = this.parseInteger(d.getAttributes(), "z");
				int heading = this.parseInteger(d.getAttributes(), "heading", 0);
				double chance = this.parseDouble(d.getAttributes(), "chance");
				npcTemplate.addSpawnLocation(new ChanceLocation(x, y, z, heading, chance));
			}
		}
	}

	public void init()
	{
		if (!DevelopmentConfig.NO_SPAWNS)
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Initializing spawns...");
			if (ThreadConfig.THREADS_FOR_LOADING)
			{
				Collection<ScheduledFuture<?>> jobs = ConcurrentHashMap.newKeySet();

				for (SpawnTemplate template : this._spawnTemplates)
				{
					if (template.isSpawningByDefault())
					{
						jobs.add(ThreadPool.schedule(() -> {
							template.spawnAll(null);
							template.notifyActivate();
						}, 0L));
					}
				}

				while (!jobs.isEmpty())
				{
					for (ScheduledFuture<?> job : jobs)
					{
						if (job == null || job.isDone() || job.isCancelled())
						{
							jobs.remove(job);
						}
					}
				}
			}
			else
			{
				for (SpawnTemplate templatex : this._spawnTemplates)
				{
					if (templatex.isSpawningByDefault())
					{
						templatex.spawnAll(null);
						templatex.notifyActivate();
					}
				}
			}

			LOGGER.info(this.getClass().getSimpleName() + ": All spawns has been initialized!");
		}
	}

	public void despawnAll()
	{
		LOGGER.info(this.getClass().getSimpleName() + ": Removing all spawns...");
		this._spawnTemplates.forEach(SpawnTemplate::despawnAll);
		LOGGER.info(this.getClass().getSimpleName() + ": All spawns has been removed!");
	}

	public Collection<SpawnTemplate> getSpawns()
	{
		return this._spawnTemplates;
	}

	public List<SpawnTemplate> getSpawns(Predicate<SpawnTemplate> condition)
	{
		List<SpawnTemplate> result = new ArrayList<>();

		for (SpawnTemplate spawnTemplate : this._spawnTemplates)
		{
			if (condition.test(spawnTemplate))
			{
				result.add(spawnTemplate);
			}
		}

		return result;
	}

	public SpawnTemplate getSpawnByName(String name)
	{
		for (SpawnTemplate spawn : this._spawnTemplates)
		{
			if (spawn.getName() != null && spawn.getName().equalsIgnoreCase(name))
			{
				return spawn;
			}
		}

		return null;
	}

	public SpawnGroup getSpawnGroupByName(String name)
	{
		for (SpawnTemplate spawnTemplate : this._spawnTemplates)
		{
			for (SpawnGroup group : spawnTemplate.getGroups())
			{
				if (group.getName() != null && group.getName().equalsIgnoreCase(name))
				{
					return group;
				}
			}
		}

		return null;
	}

	public List<NpcSpawnTemplate> getNpcSpawns(Predicate<NpcSpawnTemplate> condition)
	{
		List<NpcSpawnTemplate> result = new ArrayList<>();

		for (SpawnTemplate template : this._spawnTemplates)
		{
			for (SpawnGroup group : template.getGroups())
			{
				for (NpcSpawnTemplate spawn : group.getSpawns())
				{
					if (condition.test(spawn))
					{
						result.add(spawn);
					}
				}
			}
		}

		return result;
	}

	public synchronized void addNewSpawn(Spawn spawn)
	{
		SpawnTable.getInstance().addSpawn(spawn);
		File outputDirectory = new File("data/spawns/Others");
		if (!outputDirectory.exists())
		{
			boolean result = false;

			try
			{
				outputDirectory.mkdir();
				result = true;
			}
			catch (SecurityException var21)
			{
			}

			if (result)
			{
				LOGGER.info(this.getClass().getSimpleName() + ": Created directory: data/spawns/Others");
			}
		}

		int x = (spawn.getX() - -294912 >> 15) + 11;
		int y = (spawn.getY() - -262144 >> 15) + 10;
		File spawnFile = new File("data/spawns/Others/" + x + "_" + y + ".xml");
		String spawnId = String.valueOf(spawn.getId());
		String spawnCount = String.valueOf(spawn.getAmount());
		String spawnX = String.valueOf(spawn.getX());
		String spawnY = String.valueOf(spawn.getY());
		String spawnZ = String.valueOf(spawn.getZ());
		String spawnHeading = String.valueOf(spawn.getHeading());
		String spawnDelay = String.valueOf(spawn.getRespawnDelay() / 1000);
		if (spawnFile.exists())
		{
			File tempFile = new File(spawnFile.getAbsolutePath().substring(ServerConfig.DATAPACK_ROOT.getAbsolutePath().length() + 1).replace('\\', '/') + ".tmp");

			try
			{
				BufferedReader reader = new BufferedReader(new FileReader(spawnFile));
				BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

				String currentLine;
				while ((currentLine = reader.readLine()) != null)
				{
					if (currentLine.contains("</group>"))
					{
						NpcTemplate template = NpcData.getInstance().getTemplate(spawn.getId());
						String title = template.getTitle();
						String name = title.isEmpty() ? template.getName() : template.getName() + " - " + title;
						writer.write("\t\t\t<npc id=\"" + spawnId + (spawn.getAmount() > 1 ? "\" count=\"" + spawnCount : "") + "\" x=\"" + spawnX + "\" y=\"" + spawnY + "\" z=\"" + spawnZ + (spawn.getHeading() > 0 ? "\" heading=\"" + spawnHeading : "") + "\" respawnTime=\"" + spawnDelay + "sec\" /> <!-- " + name + " -->" + System.lineSeparator());
						writer.write(currentLine + System.lineSeparator());
					}
					else
					{
						writer.write(currentLine + System.lineSeparator());
					}
				}

				writer.close();
				reader.close();
				spawnFile.delete();
				tempFile.renameTo(spawnFile);
			}
			catch (Exception var22)
			{
				LOGGER.warning(this.getClass().getSimpleName() + ": Could not store spawn in the spawn XML files: " + var22);
			}
		}
		else
		{
			try
			{
				NpcTemplate template = NpcData.getInstance().getTemplate(spawn.getId());
				String title = template.getTitle();
				String name = title.isEmpty() ? template.getName() : template.getName() + " - " + title;
				BufferedWriter writer = new BufferedWriter(new FileWriter(spawnFile));
				writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator());
				writer.write("<list xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"../../xsd/spawns.xsd\">" + System.lineSeparator());
				writer.write("\t<spawn name=\"" + x + "_" + y + "\">" + System.lineSeparator());
				writer.write("\t\t<group>" + System.lineSeparator());
				writer.write("\t\t\t<npc id=\"" + spawnId + (spawn.getAmount() > 1 ? "\" count=\"" + spawnCount : "") + "\" x=\"" + spawnX + "\" y=\"" + spawnY + "\" z=\"" + spawnZ + (spawn.getHeading() > 0 ? "\" heading=\"" + spawnHeading : "") + "\" respawnTime=\"" + spawnDelay + "sec\" /> <!-- " + name + " -->" + System.lineSeparator());
				writer.write("\t\t</group>" + System.lineSeparator());
				writer.write("\t</spawn>" + System.lineSeparator());
				writer.write("</list>" + System.lineSeparator());
				writer.close();
				LOGGER.info(this.getClass().getSimpleName() + ": Created file: data/spawns/Others/" + x + "_" + y + ".xml");
			}
			catch (Exception var20)
			{
				LOGGER.warning(this.getClass().getSimpleName() + ": Spawn " + spawn + " could not be added to the spawn XML files: " + var20);
			}
		}
	}

	public synchronized void deleteSpawn(Spawn spawn)
	{
		SpawnTable.getInstance().removeSpawn(spawn);
		int x = (spawn.getX() - -294912 >> 15) + 11;
		int y = (spawn.getY() - -262144 >> 15) + 10;
		NpcSpawnTemplate npcSpawnTemplate = spawn.getNpcSpawnTemplate();
		File spawnFile = npcSpawnTemplate != null ? npcSpawnTemplate.getSpawnTemplate().getFile() : new File("data/spawns/Others/" + x + "_" + y + ".xml");
		File tempFile = new File(spawnFile.getAbsolutePath().substring(ServerConfig.DATAPACK_ROOT.getAbsolutePath().length() + 1).replace('\\', '/') + ".tmp");

		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(spawnFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
			boolean found = false;
			boolean isMultiLine = false;
			boolean lastLineFound = false;
			int lineCount = 0;
			SpawnGroup group = npcSpawnTemplate != null ? npcSpawnTemplate.getGroup() : null;
			List<SpawnTerritory> territories = group != null ? group.getTerritories() : Collections.emptyList();
			boolean simpleTerritory = false;
			if (territories.isEmpty())
			{
				SpawnTemplate spawnTemplate = npcSpawnTemplate != null ? npcSpawnTemplate.getSpawnTemplate() : null;
				if (spawnTemplate != null)
				{
					territories = spawnTemplate.getTerritories();
					simpleTerritory = true;
				}
			}

			String currentLine;
			if (territories.isEmpty())
			{
				String spawnId = String.valueOf(spawn.getId());
				String spawnX = String.valueOf(npcSpawnTemplate != null ? npcSpawnTemplate.getSpawnLocation().getX() : spawn.getX());
				String spawnY = String.valueOf(npcSpawnTemplate != null ? npcSpawnTemplate.getSpawnLocation().getY() : spawn.getY());
				String spawnZ = String.valueOf(npcSpawnTemplate != null ? npcSpawnTemplate.getSpawnLocation().getZ() : spawn.getZ());

				while ((currentLine = reader.readLine()) != null)
				{
					if (!found)
					{
						if (isMultiLine)
						{
							if (currentLine.contains("</npc>"))
							{
								found = true;
							}
							continue;
						}

						if (currentLine.contains(spawnId) && currentLine.contains(spawnX) && currentLine.contains(spawnY) && currentLine.contains(spawnZ))
						{
							if (!currentLine.contains("/>") && !currentLine.contains("</npc>"))
							{
								isMultiLine = true;
							}
							else
							{
								found = true;
							}
							continue;
						}
					}

					writer.write(currentLine + System.lineSeparator());
					if (currentLine.contains("</list>"))
					{
						lastLineFound = true;
					}

					if (!lastLineFound)
					{
						lineCount++;
					}
				}
			}
			else
			{
				label136:
				while ((currentLine = reader.readLine()) != null)
				{
					if (!found)
					{
						if (isMultiLine)
						{
							if (currentLine.contains("</group>") || simpleTerritory && currentLine.contains("<territories>"))
							{
								found = true;
							}
							continue;
						}

						for (SpawnTerritory territory : territories)
						{
							if (currentLine.contains("\"" + territory.getName() + "\""))
							{
								isMultiLine = true;
								continue label136;
							}
						}
					}

					writer.write(currentLine + System.lineSeparator());
					if (currentLine.contains("</list>"))
					{
						lastLineFound = true;
					}

					if (!lastLineFound)
					{
						lineCount++;
					}
				}
			}

			writer.close();
			reader.close();
			spawnFile.delete();
			tempFile.renameTo(spawnFile);
			if (lineCount < 8)
			{
				LOGGER.info(this.getClass().getSimpleName() + ": Deleted empty file: " + spawnFile.getAbsolutePath().substring(ServerConfig.DATAPACK_ROOT.getAbsolutePath().length() + 1).replace('\\', '/'));
				spawnFile.delete();
			}
		}
		catch (Exception var21)
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Spawn " + spawn + " could not be removed from the spawn XML files: " + var21);
		}
	}

	public static SpawnData getInstance()
	{
		return SpawnData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final SpawnData INSTANCE = new SpawnData();
	}
}
