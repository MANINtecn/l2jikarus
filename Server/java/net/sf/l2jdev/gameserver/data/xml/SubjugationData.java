package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.data.holders.SubjugationHolder;
import net.sf.l2jdev.gameserver.model.StatSet;
import org.w3c.dom.Document;

public class SubjugationData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(SubjugationData.class.getName());
	private static final List<SubjugationHolder> _subjugations = new ArrayList<>();

	public SubjugationData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		_subjugations.clear();
		this.parseDatapackFile("data/SubjugationData.xml");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + _subjugations.size() + " data.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", listNode -> this.forEach(listNode, "purge", purgeNode -> {
			StatSet set = new StatSet(this.parseAttributes(purgeNode));
			int category = set.getInt("category");
			List<int[]> hottimes = Arrays.stream(set.getString("hottimes").split(";")).map(it -> Arrays.stream(it.split("-")).mapToInt(Integer::parseInt).toArray()).collect(Collectors.toList());
			Map<Integer, Integer> npcs = new HashMap<>();
			this.forEach(purgeNode, "npc", npcNode -> {
				StatSet stats = new StatSet(this.parseAttributes(npcNode));
				int npcId = stats.getInt("id");
				int points = stats.getInt("points");
				npcs.put(npcId, points);
			});
			_subjugations.add(new SubjugationHolder(category, hottimes, npcs));
		}));
	}

	public SubjugationHolder getSubjugation(int category)
	{
		return _subjugations.stream().filter(it -> it.getCategory() == category).findFirst().orElse(null);
	}

	public static SubjugationData getInstance()
	{
		return SubjugationData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final SubjugationData INSTANCE = new SubjugationData();
	}
}
