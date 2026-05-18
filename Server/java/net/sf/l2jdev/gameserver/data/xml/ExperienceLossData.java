package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

public class ExperienceLossData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(ExperienceLossData.class.getName());
	private final double[] _levelPercentLost = new double[PlayerConfig.PLAYER_MAXIMUM_LEVEL];

	protected ExperienceLossData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		Arrays.fill(this._levelPercentLost, 1.0);
		this.parseDatapackFile("data/stats/players/experienceLoss.xml");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + (this._levelPercentLost.length - 1) + " levels.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", listNode -> this.forEach(listNode, "experience", experienceNode -> {
			NamedNodeMap attrs = experienceNode.getAttributes();
			int level = this.parseInteger(attrs, "level");
			if (level < PlayerConfig.PLAYER_MAXIMUM_LEVEL)
			{
				this._levelPercentLost[level] = this.parseDouble(attrs, "val");
			}
		}));
	}

	public double getPercentLost(int level)
	{
		if (level >= PlayerConfig.PLAYER_MAXIMUM_LEVEL)
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Requested too high level (" + level + ").");
			return this._levelPercentLost[this._levelPercentLost.length - 1];
		}
		return this._levelPercentLost[level];
	}

	public static ExperienceLossData getInstance()
	{
		return ExperienceLossData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final ExperienceLossData INSTANCE = new ExperienceLossData();
	}
}
