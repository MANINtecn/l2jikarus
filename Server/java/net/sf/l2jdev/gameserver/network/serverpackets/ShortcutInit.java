package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.VariationInstance;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.player.Shortcut;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ShortcutInit extends ServerPacket
{
	private final Player _player;
	private final Collection<Shortcut> _shortcuts;

	public ShortcutInit(Player player)
	{
		this._player = player;
		this._shortcuts = player.getAllShortcuts();
		player.restoreAutoShortcutVisual();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.INIT_SHORTCUT.writeId(this, buffer);
		buffer.writeInt(this._shortcuts.size());

		for (Shortcut sc : this._shortcuts)
		{
			buffer.writeInt(sc.getType().ordinal());
			buffer.writeInt(sc.getSlot() + sc.getPage() * 12);
			buffer.writeByte(sc.isAutoUse());
			switch (sc.getType())
			{
				case ITEM:
					buffer.writeInt(sc.getId());
					buffer.writeInt(1);
					buffer.writeInt(sc.getSharedReuseGroup());
					buffer.writeInt(0);
					buffer.writeInt(0);
					Item item = this._player.getInventory().getItemByObjectId(sc.getId());
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
					buffer.writeInt(sc.getId());
					buffer.writeShort(sc.getLevel());
					buffer.writeShort(sc.getSubLevel());
					buffer.writeInt(sc.getSharedReuseGroup());
					buffer.writeByte(0);
					buffer.writeInt(1);
					break;
				case ACTION:
				case MACRO:
				case RECIPE:
				case BOOKMARK:
					buffer.writeInt(sc.getId());
					buffer.writeInt(1);
			}
		}
	}
}
