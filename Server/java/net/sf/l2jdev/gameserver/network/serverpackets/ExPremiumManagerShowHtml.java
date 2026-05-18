package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.enums.HtmlActionScope;

public class ExPremiumManagerShowHtml extends AbstractHtmlPacket
{
	public ExPremiumManagerShowHtml(String html)
	{
		super(html);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PREMIUM_MANAGER_SHOW_HTML.writeId(this, buffer);
		buffer.writeInt(this.getNpcObjId());
		buffer.writeString(this.getHtml());
		buffer.writeInt(-1);
		buffer.writeInt(0);
	}

	@Override
	public HtmlActionScope getScope()
	{
		return HtmlActionScope.PREMIUM_HTML;
	}
}
