package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.enums.HtmlActionScope;

public class NpcHtmlMessage extends AbstractHtmlPacket
{
	private final int _itemId;
	private final int _size;

	public NpcHtmlMessage()
	{
		this._itemId = 0;
		this._size = 0;
	}

	public NpcHtmlMessage(int npcObjId)
	{
		super(npcObjId);
		this._itemId = 0;
		this._size = 0;
	}

	public NpcHtmlMessage(String html)
	{
		super(html);
		this._itemId = 0;
		this._size = 0;
	}

	public NpcHtmlMessage(int npcObjId, String html)
	{
		super(npcObjId, html);
		this._itemId = 0;
		this._size = 0;
	}

	public NpcHtmlMessage(int npcObjId, int itemId)
	{
		super(npcObjId);
		if (itemId < 0)
		{
			throw new IllegalArgumentException();
		}
		this._itemId = itemId;
		this._size = 0;
	}

	public NpcHtmlMessage(int npcObjId, int itemId, String html)
	{
		super(npcObjId, html);
		if (itemId < 0)
		{
			throw new IllegalArgumentException();
		}
		this._itemId = itemId;
		this._size = 0;
	}

	public NpcHtmlMessage(int npcObjId, int itemId, String html, int windowSize)
	{
		super(npcObjId, html);
		if (itemId < 0)
		{
			throw new IllegalArgumentException();
		}
		this._itemId = itemId;
		this._size = windowSize;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.NPC_HTML_MESSAGE.writeId(this, buffer);
		buffer.writeInt(this.getNpcObjId());
		buffer.writeString(this.getHtml());
		buffer.writeInt(this._itemId);
		buffer.writeInt(0);
		buffer.writeByte(this._size);
	}

	@Override
	public HtmlActionScope getScope()
	{
		return this._itemId == 0 ? HtmlActionScope.NPC_HTML : HtmlActionScope.NPC_ITEM_HTML;
	}
}
