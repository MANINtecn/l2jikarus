package net.sf.l2jdev.gameserver.model.actor.holders.player;

public class ChallengePointInfoHolder
{
	private final int _pointGroupId;
	private int _challengePoint;
	private final int _ticketPointOpt1;
	private final int _ticketPointOpt2;
	private final int _ticketPointOpt3;
	private final int _ticketPointOpt4;
	private final int _ticketPointOpt5;
	private final int _ticketPointOpt6;

	public ChallengePointInfoHolder(int pointGroupId, int challengePoint, int ticketPointOpt1, int ticketPointOpt2, int ticketPointOpt3, int ticketPointOpt4, int ticketPointOpt5, int ticketPointOpt6)
	{
		this._pointGroupId = pointGroupId;
		this._challengePoint = challengePoint;
		this._ticketPointOpt1 = ticketPointOpt1;
		this._ticketPointOpt2 = ticketPointOpt2;
		this._ticketPointOpt3 = ticketPointOpt3;
		this._ticketPointOpt4 = ticketPointOpt4;
		this._ticketPointOpt5 = ticketPointOpt5;
		this._ticketPointOpt6 = ticketPointOpt6;
	}

	public int getPointGroupId()
	{
		return this._pointGroupId;
	}

	public int getChallengePoint()
	{
		return this._challengePoint;
	}

	public int getTicketPointOpt1()
	{
		return this._ticketPointOpt1;
	}

	public int getTicketPointOpt2()
	{
		return this._ticketPointOpt2;
	}

	public int getTicketPointOpt3()
	{
		return this._ticketPointOpt3;
	}

	public int getTicketPointOpt4()
	{
		return this._ticketPointOpt4;
	}

	public int getTicketPointOpt5()
	{
		return this._ticketPointOpt5;
	}

	public int getTicketPointOpt6()
	{
		return this._ticketPointOpt6;
	}

	public void addPoints(int points)
	{
		this._challengePoint += points;
	}
}
