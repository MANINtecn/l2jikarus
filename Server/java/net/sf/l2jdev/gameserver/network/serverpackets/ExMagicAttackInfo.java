package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExMagicAttackInfo extends ServerPacket
{
	public static final int OVERHIT = 1;
	public static final int EVADED = 2;
	public static final int BLOCKED = 3;
	public static final int RESISTED = 4;
	public static final int IMMUNE = 5;
	public static final int IMMUNE2 = 6;
	public static final int CRITICAL = 7;
	public static final int CRITICAL_HEAL = 8;
	public static final int PERFECTION = 9;
	public static final int P_CRITICAL = 10;
	public static final int M_CRITICAL = 11;
	private final int _caster;
	private final int _target;
	private final int _type;

	public ExMagicAttackInfo(int caster, int target, int type)
	{
		this._caster = caster;
		this._target = target;
		this._type = type;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_MAGIC_ATTACK_INFO.writeId(this, buffer);
		buffer.writeInt(this._caster);
		buffer.writeInt(this._target);
		buffer.writeInt(this._type);
	}
}
