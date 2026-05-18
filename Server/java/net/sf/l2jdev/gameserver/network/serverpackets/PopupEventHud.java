package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class PopupEventHud extends ServerPacket
{
	public static final int MONSTER_INVASION_EVIL_MOSTERS_LEGION = 1;
	public static final int MONSTER_INVASION_OL_MAHUM_ALLIANCE = 2;
	public static final int MONSTER_INVASION_FURIOUS_LIZARDMENS_LEGION = 3;
	public static final int MONSTER_INVASION_BRUTAL_MONSTERS_LEGION = 4;
	public static final int MONSTER_INVASION_TANTAR_LIZARDMEN_TRIBE = 5;
	public static final int LEGION_OF_DARKNESS_INVASION_PLAINS_OF_LIZARDMEN = 6;
	public static final int LEGION_OF_DARKNESS_INVASION_WESTERN_DRAGON_VALLEY = 7;
	public static final int LEGION_OF_DARKNESS_INVASION_EASTERN_DRAGON_VALLEY = 8;
	public static final int LEGION_OF_DARKNESS_INVASION_MELAT_LIZARDMEN = 9;
	public static final int LEGION_OF_DARKNESS_INVASION_TAYGA_CAMP = 10;
	public static final int DEATHMATCH_WITH_THE_LEGION_OF_DARKNESS_GIRAN_CASTLE = 11;
	public static final int WATERMELON_SORTING_WEST_TALKING_ISLAND = 12;
	public static final int FINAL_BLOW_TO_WATERMELON_FANTASY_ISLE = 13;
	public static final int CATACOMBS = 14;
	public static final int MONSTER_INVASION_SHIFTING_MIRAGE = 15;
	private final int _id;
	private final boolean _enabled;

	public PopupEventHud(int id, boolean enabled)
	{
		this._id = id;
		this._enabled = enabled;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_POPUP_EVENT_HUD.writeId(this, buffer);
		buffer.writeInt(this._id);
		buffer.writeByte(this._enabled);
	}
}
