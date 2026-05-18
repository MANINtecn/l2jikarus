package net.sf.l2jdev.gameserver.network.serverpackets.newhenna;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.xml.HennaPatternPotentialData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.henna.Henna;
import net.sf.l2jdev.gameserver.model.item.henna.HennaPoten;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class NewHennaList extends ServerPacket
{
	private final HennaPoten[] _hennaId;
	private final int _dailyStep;
	private final int _dailyCount;
	private final int _availableSlots;
	private final int _resetCount;
	private final int _sendType;
	private List<ItemHolder> _resetData = new ArrayList<>();

	public NewHennaList(Player player, int sendType)
	{
		this._dailyStep = player.getDyePotentialDailyStep();
		this._dailyCount = player.getDyePotentialDailyCount();
		this._hennaId = player.getHennaPotenList();
		this._availableSlots = player.getAvailableHennaSlots();
		this._resetCount = player.getDyePotentialDailyEnchantReset();
		this._resetData = HennaPatternPotentialData.getInstance().getEnchantReset();
		this._sendType = sendType;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_NEW_HENNA_LIST.writeId(this, buffer);
		buffer.writeByte(this._sendType);
		buffer.writeShort(this._dailyStep);
		buffer.writeShort(this._dailyCount);
		buffer.writeShort(this._resetCount + 1);
		buffer.writeShort(-1);
		buffer.writeInt(this._resetData.size());

		for (ItemHolder resetInfo : this._resetData)
		{
			buffer.writeInt(resetInfo.getId());
			buffer.writeLong(resetInfo.getCount());
		}

		buffer.writeInt(this._hennaId.length);

		for (int i = 1; i <= this._hennaId.length; i++)
		{
			HennaPoten hennaPoten = this._hennaId[i - 1];
			Henna henna = this._hennaId[i - 1].getHenna();
			buffer.writeInt(henna != null ? henna.getDyeId() : 0);
			buffer.writeInt(hennaPoten.getPotenId());
			buffer.writeByte(i != this._availableSlots);
			buffer.writeShort(hennaPoten.getEnchantLevel());
			buffer.writeInt(hennaPoten.getEnchantExp());
			buffer.writeShort(hennaPoten.getActiveStep());
			buffer.writeShort(this._dailyStep);
			buffer.writeShort(this._dailyCount);
			buffer.writeShort(hennaPoten.getUnlockSlot());
		}
	}
}
