package net.sf.l2jdev.gameserver.model.skill.holders;

public class TemplateChanceHolder
{
	private final int _templateId;
	private final int _minChance;
	private final int _maxChance;

	public TemplateChanceHolder(int templateId, int minChance, int maxChance)
	{
		this._templateId = templateId;
		this._minChance = minChance;
		this._maxChance = maxChance;
	}

	public int getTemplateId()
	{
		return this._templateId;
	}

	public boolean calcChance(int chance)
	{
		return this._maxChance > chance && chance >= this._minChance;
	}

	@Override
	public String toString()
	{
		return "[TemplateId: " + this._templateId + " minChance: " + this._minChance + " maxChance: " + this._minChance + "]";
	}
}
