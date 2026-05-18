package net.sf.l2jdev.gameserver.network.serverpackets.pledgebonus;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.clan.enums.ClanRewardType;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPledgeBonusUpdate extends ServerPacket
{
	private final ClanRewardType _type;
	private final int _value;

	public ExPledgeBonusUpdate(ClanRewardType type, int value)
	{
		this._type = type;
		this._value = value;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PLEDGE_BONUS_UPDATE.writeId(this, buffer);
		buffer.writeByte(this._type.getClientId());
		buffer.writeInt(this._value);
	}
}
