package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExDamagePopUp extends ServerPacket
{
	public static final byte NOMAKE = 0;
	public static final byte NORMAL_ATTACK = 1;
	public static final byte CONSECUTIVE_ATTACK = 2;
	public static final byte CRITICAL = 3;
	public static final byte OVERHIT = 4;
	public static final byte RECOVER_HP = 5;
	public static final byte RECOVER_MP = 6;
	public static final byte GET_SP = 7;
	public static final byte GET_EXP = 8;
	public static final byte MAGIC_DEFIANCE = 9;
	public static final byte SHIELD_GUARD = 10;
	public static final byte DODGE = 11;
	public static final byte IMMUNE = 12;
	public static final byte SKILL_HIT = 13;
	public static final byte RECOVER_CP = 14;
	public static final byte PHYSICAL_CRITICAL = 15;
	public static final byte MAGIC_CRITICAL = 16;
	public static final byte SKILL_EVADES = 17;
	public static final byte GET_EXP_BY_MAGIC_LAMP = 18;
	public static final byte GET_SP_BY_MAGIC_LAMP = 19;
	public static final byte ETC = 100;
	private final int _caster;
	private final int _target;
	private final int _damage;
	private final byte _type;

	public ExDamagePopUp(int caster, int target, int damage, byte type)
	{
		this._caster = caster;
		this._target = target;
		this._damage = damage;
		this._type = type;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_DAMAGE_POPUP.writeId(this, buffer);
		buffer.writeInt(this._caster);
		buffer.writeInt(this._target);
		buffer.writeInt(-this._damage);
		buffer.writeByte(this._type);
		buffer.writeInt(0);
		buffer.writeInt(0);
	}
}
