package net.sf.l2jdev.gameserver.model.skill;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum SkillConditionScope
{
	GENERAL("conditions"),
	TARGET("targetConditions"),
	PASSIVE("passiveConditions");

	private static final Map<String, SkillConditionScope> XML_NODE_NAME_TO_SKILL_CONDITION_SCOPE = Arrays.stream(values()).collect(Collectors.toMap(e -> e.getXmlNodeName(), e -> e));
	private final String _xmlNodeName;

	private SkillConditionScope(String xmlNodeName)
	{
		this._xmlNodeName = xmlNodeName;
	}

	public String getXmlNodeName()
	{
		return this._xmlNodeName;
	}

	public static SkillConditionScope findByXmlNodeName(String xmlNodeName)
	{
		return XML_NODE_NAME_TO_SKILL_CONDITION_SCOPE.get(xmlNodeName);
	}
}
