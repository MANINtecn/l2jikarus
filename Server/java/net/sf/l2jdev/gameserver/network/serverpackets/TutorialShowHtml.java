package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.enums.HtmlActionScope;

public class TutorialShowHtml extends AbstractHtmlPacket
{
	public static final int NORMAL_WINDOW = 1;
	public static final int LARGE_WINDOW = 2;
	private final int _type;

	public TutorialShowHtml(String html)
	{
		super(html);
		this._type = 1;
	}

	public TutorialShowHtml(int npcObjId, String html, int type)
	{
		super(npcObjId, html);
		this._type = type;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.TUTORIAL_SHOW_HTML.writeId(this, buffer);
		buffer.writeInt(this._type);
		buffer.writeString(this.getHtml());
	}

	@Override
	public HtmlActionScope getScope()
	{
		return HtmlActionScope.TUTORIAL_HTML;
	}
}
