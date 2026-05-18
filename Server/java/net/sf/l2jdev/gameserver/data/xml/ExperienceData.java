package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.config.PlayerConfig;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

public class ExperienceData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(ExperienceData.class.getName());
	private long[] _expTable;
	private double[] _trainingRateTable;
	private int _maxLevel;
	private int _maxPetLevel;

	protected ExperienceData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this.parseDatapackFile("data/stats/players/experience.xml");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + (this._expTable.length - 1) + " levels.");
		LOGGER.info(this.getClass().getSimpleName() + ": Max Player Level is " + (this._maxLevel - 1) + ".");
		LOGGER.info(this.getClass().getSimpleName() + ": Max Pet Level is " + (this._maxPetLevel - 1) + ".");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, tableNode -> {
			NamedNodeMap tableAttr = tableNode.getAttributes();
			this._maxLevel = Integer.parseInt(tableAttr.getNamedItem("maxLevel").getNodeValue()) + 1;
			this._maxPetLevel = Integer.parseInt(tableAttr.getNamedItem("maxPetLevel").getNodeValue()) + 1;
			if (this._maxLevel > PlayerConfig.PLAYER_MAXIMUM_LEVEL)
			{
				this._maxLevel = PlayerConfig.PLAYER_MAXIMUM_LEVEL;
			}

			if (this._maxPetLevel > this._maxLevel + 1)
			{
				this._maxPetLevel = this._maxLevel + 1;
			}

			this._expTable = new long[this._maxLevel + 1];
			this._trainingRateTable = new double[this._maxLevel + 1];
			this.forEach(tableNode, "experience", experienceNode -> {
				NamedNodeMap attrs = experienceNode.getAttributes();
				int level = this.parseInteger(attrs, "level");
				if (level <= PlayerConfig.PLAYER_MAXIMUM_LEVEL && level <= this._maxLevel)
				{
					this._expTable[level] = this.parseLong(attrs, "tolevel");
					this._trainingRateTable[level] = this.parseDouble(attrs, "trainingRate");
				}
			});
		});
	}

	public long getExpForLevel(int level)
	{
		return level > PlayerConfig.PLAYER_MAXIMUM_LEVEL ? this._expTable[PlayerConfig.PLAYER_MAXIMUM_LEVEL] : this._expTable[level];
	}

	public double getTrainingRate(int level)
	{
		return level > PlayerConfig.PLAYER_MAXIMUM_LEVEL ? this._trainingRateTable[PlayerConfig.PLAYER_MAXIMUM_LEVEL] : this._trainingRateTable[level];
	}

	public int getMaxLevel()
	{
		return this._maxLevel;
	}

	public int getMaxPetLevel()
	{
		return this._maxPetLevel;
	}

	public static ExperienceData getInstance()
	{
		return ExperienceData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final ExperienceData INSTANCE = new ExperienceData();
	}
}
