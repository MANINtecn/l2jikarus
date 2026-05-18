package net.sf.l2jdev.gameserver.model.skill;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum EffectScope
{
	GENERAL("effects"),
	START("startEffects"),
	SELF("selfEffects"),
	CHANNELING("channelingEffects"),
	PVP("pvpEffects"),
	PVE("pveEffects"),
	END("endEffects");

	private static final Map<String, EffectScope> XML_NODE_NAME_TO_EFFECT_SCOPE = Arrays.stream(values()).collect(Collectors.toMap(e -> e.getXmlNodeName(), e -> e));
	private final String _xmlNodeName;

	private EffectScope(String xmlNodeName)
	{
		this._xmlNodeName = xmlNodeName;
	}

	public String getXmlNodeName()
	{
		return this._xmlNodeName;
	}

	public static EffectScope findByXmlNodeName(String xmlNodeName)
	{
		return XML_NODE_NAME_TO_EFFECT_SCOPE.get(xmlNodeName);
	}
}
