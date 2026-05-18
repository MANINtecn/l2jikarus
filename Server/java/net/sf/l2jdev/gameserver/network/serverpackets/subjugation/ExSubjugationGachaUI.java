package net.sf.l2jdev.gameserver.network.serverpackets.subjugation;

import java.util.Collection;
import java.util.Map;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.xml.SubjugationGacha;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExSubjugationGachaUI extends ServerPacket
{
	private final int _category;
	private final int _keyCount;

	public ExSubjugationGachaUI(int category, int keyCount)
	{
		this._category = category;
		this._keyCount = keyCount;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SUBJUGATION_GACHA_UI.writeId(this, buffer);
		buffer.writeInt(this._category);
		buffer.writeInt(this._keyCount);
		Map<Integer, Double> subjugationData = SubjugationGacha.getInstance().getSubjugation(this._category);
		if (subjugationData == null)
		{
			buffer.writeInt(0);
		}
		else
		{
			Collection<Double> values = subjugationData.values();
			buffer.writeInt(values.size());

			for (Double chance : values)
			{
				buffer.writeInt((int) (chance * 100.0));
			}
		}
	}
}
