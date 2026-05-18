package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.Position;
import net.sf.l2jdev.gameserver.taskmanagers.GameTimeTaskManager;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class HitConditionBonusData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(HitConditionBonusData.class.getName());
	private int _frontBonus = 0;
	private int _sideBonus = 0;
	private int _backBonus = 0;
	private int _highBonus = 0;
	private int _lowBonus = 0;
	private int _darkBonus = 0;

	protected HitConditionBonusData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this.parseDatapackFile("data/stats/hitConditionBonus.xml");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded hit condition bonuses.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		for (Node d = document.getFirstChild().getFirstChild(); d != null; d = d.getNextSibling())
		{
			NamedNodeMap attrs = d.getAttributes();
			String var5 = d.getNodeName();
			switch (var5)
			{
				case "front":
					this._frontBonus = this.parseInteger(attrs, "val");
					break;
				case "side":
					this._sideBonus = this.parseInteger(attrs, "val");
					break;
				case "back":
					this._backBonus = this.parseInteger(attrs, "val");
					break;
				case "high":
					this._highBonus = this.parseInteger(attrs, "val");
					break;
				case "low":
					this._lowBonus = this.parseInteger(attrs, "val");
					break;
				case "dark":
					this._darkBonus = this.parseInteger(attrs, "val");
			}
		}
	}

	public double getConditionBonus(Creature attacker, Creature target)
	{
		double mod = 100.0;
		int heightDifference = attacker.getZ() - target.getZ();
		if (heightDifference > 50)
		{
			mod += this._highBonus;
		}
		else if (heightDifference < -50)
		{
			mod += this._lowBonus;
		}

		if (GameTimeTaskManager.getInstance().isNight())
		{
			mod += this._darkBonus;
		}
		return Math.max(switch (Position.getPosition(attacker, target))
		{
			case SIDE -> mod + this._sideBonus;
			case BACK -> mod + this._backBonus;
			default -> mod + this._frontBonus;
		} / 100.0, 0.0);
	}

	public static HitConditionBonusData getInstance()
	{
		return HitConditionBonusData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final HitConditionBonusData INSTANCE = new HitConditionBonusData();
	}
}
