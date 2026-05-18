package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.enums.HtmlActionScope;

public class NpcQuestHtmlMessage extends AbstractHtmlPacket
{
	private final int _questId;

	public NpcQuestHtmlMessage(int npcObjId, int questId)
	{
		super(npcObjId);
		this._questId = questId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_NPC_QUEST_HTML_MESSAGE.writeId(this, buffer);
		buffer.writeInt(this.getNpcObjId());
		buffer.writeString(this.getHtml());
		buffer.writeInt(this._questId);
	}

	@Override
	public HtmlActionScope getScope()
	{
		return HtmlActionScope.NPC_QUEST_HTML;
	}
}
