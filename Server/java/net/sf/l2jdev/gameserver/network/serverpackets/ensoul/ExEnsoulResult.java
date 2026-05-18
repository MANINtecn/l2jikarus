package net.sf.l2jdev.gameserver.network.serverpackets.ensoul;

import java.util.Collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.ensoul.EnsoulOption;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExEnsoulResult extends ServerPacket
{
	private final int _success;
	private final Collection<EnsoulOption> _specialAbilities;
	private final Collection<EnsoulOption> _additionalSpecialAbilities;

	public ExEnsoulResult(int success, Item item)
	{
		this._success = success;
		this._specialAbilities = item.getSpecialAbilities();
		this._additionalSpecialAbilities = item.getAdditionalSpecialAbilities();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ENSOUL_RESULT.writeId(this, buffer);
		buffer.writeByte(this._success);
		buffer.writeByte(this._specialAbilities.size());

		for (EnsoulOption option : this._specialAbilities)
		{
			buffer.writeInt(option.getId());
		}

		buffer.writeByte(this._additionalSpecialAbilities.size());

		for (EnsoulOption option : this._additionalSpecialAbilities)
		{
			buffer.writeInt(option.getId());
		}
	}
}
