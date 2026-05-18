package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.templates.PlayerTemplate;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class NewCharacterSuccess extends ServerPacket
{
	private final List<PlayerTemplate> _chars = new ArrayList<>();

	public void addChar(PlayerTemplate template)
	{
		this._chars.add(template);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.NEW_CHARACTER_SUCCESS.writeId(this, buffer);
		buffer.writeInt(this._chars.size());

		for (PlayerTemplate chr : this._chars)
		{
			if (chr != null)
			{
				buffer.writeInt(chr.getRace().ordinal());
				buffer.writeInt(chr.getPlayerClass().getId());
				buffer.writeInt(99);
				buffer.writeInt(chr.getBaseSTR());
				buffer.writeInt(1);
				buffer.writeInt(99);
				buffer.writeInt(chr.getBaseDEX());
				buffer.writeInt(1);
				buffer.writeInt(99);
				buffer.writeInt(chr.getBaseCON());
				buffer.writeInt(1);
				buffer.writeInt(99);
				buffer.writeInt(chr.getBaseINT());
				buffer.writeInt(1);
				buffer.writeInt(99);
				buffer.writeInt(chr.getBaseWIT());
				buffer.writeInt(1);
				buffer.writeInt(99);
				buffer.writeInt(chr.getBaseMEN());
				buffer.writeInt(1);
			}
		}
	}
}
