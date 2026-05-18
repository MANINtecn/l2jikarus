package net.sf.l2jdev.gameserver.network.serverpackets.ensoul;

import java.util.Collection;
import java.util.Collections;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.ensoul.EnsoulOption;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExEnSoulExtractionResult extends ServerPacket
{
	private final boolean _success;
	private final Collection<EnsoulOption> _specialAbilities;
	private final Collection<EnsoulOption> _additionalSpecialAbilities;

	@SuppressWarnings("unchecked")
	public ExEnSoulExtractionResult(boolean success, Item item)
	{
		this._success = success;
		this._specialAbilities = (Collection<EnsoulOption>) (success ? item.getSpecialAbilities() : Collections.emptyList());
		this._additionalSpecialAbilities = (Collection<EnsoulOption>) (success ? item.getAdditionalSpecialAbilities() : Collections.emptyList());
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ENSOUL_EXTRACTION_RESULT.writeId(this, buffer);
		buffer.writeByte(this._success);
		if (this._success)
		{
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
}
