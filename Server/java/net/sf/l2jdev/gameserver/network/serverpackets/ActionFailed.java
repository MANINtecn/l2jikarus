package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.EnumMap;
import java.util.Map;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.skill.SkillCastingType;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ActionFailed extends ServerPacket
{
	public static final ActionFailed STATIC_PACKET = new ActionFailed();
	private static final Map<SkillCastingType, ActionFailed> STATIC_PACKET_BY_CASTING_TYPE = new EnumMap<>(SkillCastingType.class);
	private final int _castingType;

	private ActionFailed()
	{
		this._castingType = 0;
	}

	private ActionFailed(int castingType)
	{
		this._castingType = castingType;
	}

	public static ActionFailed get(SkillCastingType castingType)
	{
		return STATIC_PACKET_BY_CASTING_TYPE.getOrDefault(castingType, STATIC_PACKET);
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.ACTION_FAIL.writeId(this, buffer);
		buffer.writeInt(this._castingType);
	}

	static
	{
		for (SkillCastingType castingType : SkillCastingType.values())
		{
			STATIC_PACKET_BY_CASTING_TYPE.put(castingType, new ActionFailed(castingType.getClientBarId()));
		}
	}
}
