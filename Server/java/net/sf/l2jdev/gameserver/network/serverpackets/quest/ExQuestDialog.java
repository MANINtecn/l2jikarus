package net.sf.l2jdev.gameserver.network.serverpackets.quest;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.script.QuestDialogType;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExQuestDialog extends ServerPacket
{
	private final int _questId;
	private final QuestDialogType _dialogType;

	public ExQuestDialog(int questId, QuestDialogType dialogType)
	{
		this._questId = questId;
		this._dialogType = dialogType;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_QUEST_DIALOG.writeId(this, buffer);
		buffer.writeInt(this._questId);
		buffer.writeInt(this._dialogType.getId());
	}
}
