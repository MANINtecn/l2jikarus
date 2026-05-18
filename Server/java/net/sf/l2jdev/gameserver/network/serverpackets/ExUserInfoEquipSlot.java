package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.VariationInstance;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.itemcontainer.PlayerInventory;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.enums.InventorySlot;

public class ExUserInfoEquipSlot extends AbstractMaskPacket<InventorySlot>
{
	private final Player _player;
	private final byte[] _masks = new byte[]
	{
		0,
		0,
		0,
		0,
		0,
		0,
		0,
		0
	};

	public ExUserInfoEquipSlot(Player player)
	{
		this(player, true);
	}

	public ExUserInfoEquipSlot(Player player, boolean addAll)
	{
		this._player = player;
		if (addAll)
		{
			this.addComponentType(InventorySlot.values());
		}
	}

	@Override
	protected byte[] getMasks()
	{
		return this._masks;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_USER_INFO_EQUIPSLOT.writeId(this, buffer);
		buffer.writeInt(this._player.getObjectId());
		buffer.writeShort(InventorySlot.values().length);
		buffer.writeBytes(this._masks);
		PlayerInventory inventory = this._player.getInventory();

		for (InventorySlot slot : InventorySlot.values())
		{
			if (this.containsMask(slot))
			{
				VariationInstance augment = inventory.getPaperdollAugmentation(slot.getSlot());
				buffer.writeShort(26);
				buffer.writeInt(inventory.getPaperdollObjectId(slot.getSlot()));
				buffer.writeInt(inventory.getPaperdollItemId(slot.getSlot()));
				if (augment != null)
				{
					buffer.writeInt(augment.getOption1Id());
					buffer.writeInt(augment.getOption2Id());
					buffer.writeInt(augment.getOption3Id());
				}
				else
				{
					buffer.writeInt(0);
					buffer.writeInt(0);
					buffer.writeInt(0);
				}

				buffer.writeInt(inventory.getPaperdollItemVisualId(slot.getSlot()));
			}
		}
	}
}
