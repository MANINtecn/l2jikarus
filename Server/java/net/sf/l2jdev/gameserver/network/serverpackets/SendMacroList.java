package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.enums.player.MacroUpdateType;
import net.sf.l2jdev.gameserver.model.actor.holders.player.Macro;
import net.sf.l2jdev.gameserver.model.actor.holders.player.MacroCmd;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class SendMacroList extends ServerPacket
{
	private final int _count;
	private final Macro _macro;
	private final MacroUpdateType _updateType;

	public SendMacroList(int count, Macro macro, MacroUpdateType updateType)
	{
		this._count = count;
		this._macro = macro;
		this._updateType = updateType;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.MACRO_LIST.writeId(this, buffer);
		buffer.writeByte(this._updateType.getId());
		buffer.writeInt(this._updateType != MacroUpdateType.LIST ? this._macro.getId() : 0);
		buffer.writeByte(this._count);
		buffer.writeByte(this._macro != null);
		if (this._macro != null && this._updateType != MacroUpdateType.DELETE)
		{
			buffer.writeInt(this._macro.getId());
			buffer.writeString(this._macro.getName());
			buffer.writeString(this._macro.getDescr());
			buffer.writeString(this._macro.getAcronym());
			buffer.writeInt(this._macro.getIcon());
			buffer.writeByte(this._macro.getCommands().size());
			int i = 1;

			for (MacroCmd cmd : this._macro.getCommands())
			{
				buffer.writeByte(i++);
				buffer.writeByte(cmd.getType().ordinal());
				buffer.writeInt(cmd.getD1());
				buffer.writeByte(cmd.getD2());
				buffer.writeString(cmd.getCmd());
			}
		}
	}
}
