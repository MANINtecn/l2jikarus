package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.templates.CubicTemplate;
import net.sf.l2jdev.gameserver.model.cubic.CubicSkill;
import net.sf.l2jdev.gameserver.model.cubic.ICubicConditionHolder;
import net.sf.l2jdev.gameserver.model.cubic.conditions.HealthCondition;
import net.sf.l2jdev.gameserver.model.cubic.conditions.HpCondition;
import net.sf.l2jdev.gameserver.model.cubic.conditions.RangeCondition;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class CubicData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(CubicData.class.getName());
	private final Map<Integer, Map<Integer, CubicTemplate>> _cubics = new ConcurrentHashMap<>();

	protected CubicData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this._cubics.clear();
		this.parseDatapackDirectory("data/stats/cubics", true);
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._cubics.size() + " cubics.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", listNode -> this.forEach(listNode, "cubic", cubicNode -> this.parseTemplate(cubicNode, new CubicTemplate(new StatSet(this.parseAttributes(cubicNode))))));
	}

	private void parseTemplate(Node cubicNode, CubicTemplate template)
	{
		this.forEach(cubicNode, IXmlReader::isNode, innerNode -> {
			String s0$ = innerNode.getNodeName();
			switch (s0$)
			{
				case "conditions":
					this.parseConditions(innerNode, template, template);
					break;
				case "skills":
					this.parseSkills(innerNode, template);
			}
		});
		this._cubics.computeIfAbsent(template.getId(), _ -> new HashMap<>()).put(template.getLevel(), template);
	}

	private void parseConditions(Node cubicNode, CubicTemplate template, ICubicConditionHolder holder)
	{
		this.forEach(cubicNode, IXmlReader::isNode, conditionNode -> {
			String s0$ = conditionNode.getNodeName();
			switch (s0$)
			{
				case "hp":
					HpCondition.HpConditionType type = this.parseEnum(conditionNode.getAttributes(), HpCondition.HpConditionType.class, "type");
					int hpPer = this.parseInteger(conditionNode.getAttributes(), "percent");
					holder.addCondition(new HpCondition(type, hpPer));
					break;
				case "range":
					int range = this.parseInteger(conditionNode.getAttributes(), "value");
					holder.addCondition(new RangeCondition(range));
					break;
				case "healthPercent":
					int min = this.parseInteger(conditionNode.getAttributes(), "min");
					int max = this.parseInteger(conditionNode.getAttributes(), "max");
					holder.addCondition(new HealthCondition(min, max));
					break;
				default:
					LOGGER.warning("Attempting to use not implemented condition: " + conditionNode.getNodeName() + " for cubic id: " + template.getId() + " level: " + template.getLevel());
			}
		});
	}

	private void parseSkills(Node cubicNode, CubicTemplate template)
	{
		this.forEach(cubicNode, "skill", skillNode -> {
			CubicSkill skill = new CubicSkill(new StatSet(this.parseAttributes(skillNode)));
			this.forEach(cubicNode, "conditions", _ -> this.parseConditions(cubicNode, template, skill));
			template.getCubicSkills().add(skill);
		});
	}

	public CubicTemplate getCubicTemplate(int id, int level)
	{
		return this._cubics.getOrDefault(id, Collections.emptyMap()).get(level);
	}

	public static CubicData getInstance()
	{
		return CubicData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final CubicData INSTANCE = new CubicData();
	}
}
