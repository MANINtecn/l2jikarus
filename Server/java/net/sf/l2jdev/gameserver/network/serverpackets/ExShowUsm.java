package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExShowUsm extends ServerPacket
{
	public static final ExShowUsm GOD_INTRO = new ExShowUsm(2);
	public static final ExShowUsm SECOND_TRANSFER_QUEST = new ExShowUsm(4);
	public static final ExShowUsm OCTAVIS_INSTANCE_END = new ExShowUsm(6);
	public static final ExShowUsm AWAKENING_END = new ExShowUsm(10);
	public static final ExShowUsm ERTHEIA_FIRST_QUEST = new ExShowUsm(14);
	public static final ExShowUsm USM_Q015_E = new ExShowUsm(15);
	public static final ExShowUsm ERTHEIA_INTRO_FOR_ERTHEIA = new ExShowUsm(147);
	public static final ExShowUsm ERTHEIA_INTRO_FOR_OTHERS = new ExShowUsm(148);
	public static final ExShowUsm ANTHARAS_INTRO = new ExShowUsm(149);
	public static final ExShowUsm DEATH_KNIGHT_INTRO = new ExShowUsm(150);
	public static final ExShowUsm CONQUEST_INTRO = new ExShowUsm(151);
	public static final ExShowUsm SHINE_MAKER_INTRO = new ExShowUsm(152);
	private final int _videoId;

	private ExShowUsm(int videoId)
	{
		this._videoId = videoId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SEND_USM_EVENT.writeId(this, buffer);
		buffer.writeInt(this._videoId);
	}
}
