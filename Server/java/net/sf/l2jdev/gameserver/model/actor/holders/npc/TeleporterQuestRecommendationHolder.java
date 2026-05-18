package net.sf.l2jdev.gameserver.model.actor.holders.npc;

public class TeleporterQuestRecommendationHolder
{
	private final int _npcId;
	private final String _questName;
	private final int[] _conditions;
	private final String _html;

	public TeleporterQuestRecommendationHolder(int npcId, String questName, int[] conditions, String html)
	{
		this._npcId = npcId;
		this._questName = questName;
		this._conditions = conditions;
		this._html = html;
	}

	public int getNpcId()
	{
		return this._npcId;
	}

	public String getQuestName()
	{
		return this._questName;
	}

	public int[] getConditions()
	{
		return this._conditions;
	}

	public String getHtml()
	{
		return this._html;
	}
}
