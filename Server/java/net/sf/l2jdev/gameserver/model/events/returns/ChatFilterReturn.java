package net.sf.l2jdev.gameserver.model.events.returns;

import net.sf.l2jdev.gameserver.network.enums.ChatType;

public class ChatFilterReturn extends AbstractEventReturn
{
	private final String _filteredText;
	private final ChatType _chatType;

	public ChatFilterReturn(String filteredText, ChatType newChatType, boolean override, boolean abort)
	{
		super(override, abort);
		this._filteredText = filteredText;
		this._chatType = newChatType;
	}

	public String getFilteredText()
	{
		return this._filteredText;
	}

	public ChatType getChatType()
	{
		return this._chatType;
	}
}
