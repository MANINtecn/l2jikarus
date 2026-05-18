package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.VariationInstance;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.player.Shortcut;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ShortcutRegister extends ServerPacket
{
	private final Player _player;
	private final Shortcut _shortcut;

	public ShortcutRegister(Shortcut shortcut, Player player)
	{
		this._player = player;
		this._shortcut = shortcut;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.SHORTCUT_REG.writeId(this, buffer);
		buffer.writeInt(this._shortcut.getType().ordinal());
		buffer.writeInt(this._shortcut.getSlot() + this._shortcut.getPage() * 12);
		buffer.writeByte(this._shortcut.isAutoUse());
		switch (this._shortcut.getType())
		{
			case ITEM:
				buffer.writeInt(this._shortcut.getId());
				buffer.writeInt(this._shortcut.getCharacterType());
				buffer.writeInt(this._shortcut.getSharedReuseGroup());
				buffer.writeInt(0);
				buffer.writeInt(0);
				Item item = this._player.getInventory().getItemByObjectId(this._shortcut.getId());
				if (item != null)
				{
					VariationInstance augment = item.getAugmentation();
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

					buffer.writeInt(item.getVisualId());
				}
				else
				{
					buffer.writeInt(0);
					buffer.writeInt(0);
					buffer.writeInt(0);
				}
				break;
			case SKILL:
				buffer.writeInt(this._shortcut.getId());
				buffer.writeShort(this._shortcut.getLevel());
				buffer.writeShort(this._shortcut.getSubLevel());
				buffer.writeInt(this._shortcut.getSharedReuseGroup());
				buffer.writeByte(0);
				buffer.writeInt(this._shortcut.getCharacterType());
				buffer.writeInt(0);
				buffer.writeInt(0);
				break;
			case ACTION:
			case MACRO:
			case RECIPE:
			case BOOKMARK:
				buffer.writeInt(this._shortcut.getId());
				buffer.writeInt(this._shortcut.getCharacterType());
		}
	}
}
