package net.sf.l2jdev.gameserver.model;

import net.sf.l2jdev.gameserver.network.NpcStringId;

public class NpcWalkerNode extends Location
{
	private final String _chatString;
	private final NpcStringId _npcString;
	private final int _delay;
	private final boolean _runToLocation;

	public NpcWalkerNode(int moveX, int moveY, int moveZ, int delay, boolean runToLocation, NpcStringId npcString, String chatText)
	{
		super(moveX, moveY, moveZ);
		this._delay = delay;
		this._runToLocation = runToLocation;
		this._npcString = npcString;
		this._chatString = chatText == null ? "" : chatText;
	}

	public int getDelay()
	{
		return this._delay;
	}

	public boolean runToLocation()
	{
		return this._runToLocation;
	}

	public NpcStringId getNpcString()
	{
		return this._npcString;
	}

	public String getChatText()
	{
		if (this._npcString != null)
		{
			throw new IllegalStateException("npcString is defined for walker route!");
		}
		return this._chatString;
	}
}
