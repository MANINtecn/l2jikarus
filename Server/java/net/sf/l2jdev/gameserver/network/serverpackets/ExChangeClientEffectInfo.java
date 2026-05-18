package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExChangeClientEffectInfo extends ServerPacket
{
	public static final ExChangeClientEffectInfo STATIC_FREYA_DEFAULT = new ExChangeClientEffectInfo(0, 0, 1);
	public static final ExChangeClientEffectInfo STATIC_FREYA_DESTROYED = new ExChangeClientEffectInfo(0, 0, 2);
	public static final ExChangeClientEffectInfo GIRAN_NORMAL = new ExChangeClientEffectInfo(0, 0, 1);
	public static final ExChangeClientEffectInfo GIRAN_PETALS = new ExChangeClientEffectInfo(0, 0, 2);
	public static final ExChangeClientEffectInfo GIRAN_SNOW = new ExChangeClientEffectInfo(0, 0, 3);
	public static final ExChangeClientEffectInfo GIRAN_FLOWERS = new ExChangeClientEffectInfo(0, 0, 4);
	public static final ExChangeClientEffectInfo GIRAN_WATER = new ExChangeClientEffectInfo(0, 0, 5);
	public static final ExChangeClientEffectInfo GIRAN_AUTUMN = new ExChangeClientEffectInfo(0, 0, 6);
	public static final ExChangeClientEffectInfo TRANSCEDENT_DEFAULT = new ExChangeClientEffectInfo(0, 0, 1);
	public static final ExChangeClientEffectInfo TRANSCEDENT_GIRAN = new ExChangeClientEffectInfo(0, 0, 2);
	public static final ExChangeClientEffectInfo TRANSCEDENT_ORC = new ExChangeClientEffectInfo(0, 0, 3);
	public static final ExChangeClientEffectInfo TRANSCEDENT_DWARF = new ExChangeClientEffectInfo(0, 0, 4);
	public static final ExChangeClientEffectInfo TRANSCEDENT_DARK_ELF = new ExChangeClientEffectInfo(0, 0, 5);
	public static final ExChangeClientEffectInfo TRANSCEDENT_GRACIA_AIRSTRIP = new ExChangeClientEffectInfo(0, 0, 6);
	public static final ExChangeClientEffectInfo TRANSCEDENT_ELF = new ExChangeClientEffectInfo(0, 0, 7);
	public static final ExChangeClientEffectInfo TRANSCEDENT_ERTHEIA = new ExChangeClientEffectInfo(0, 0, 8);
	public static final ExChangeClientEffectInfo TRANSCEDENT_HIGH_ELF = new ExChangeClientEffectInfo(0, 0, 9);
	public static final ExChangeClientEffectInfo TRANSCEDENT_SKY_TOWER = new ExChangeClientEffectInfo(0, 0, 10);
	public static final ExChangeClientEffectInfo SEAL_OF_SHILLEN_DEFAULT = new ExChangeClientEffectInfo(0, 0, 1);
	public static final ExChangeClientEffectInfo SEAL_OF_SHILLEN_CORE = new ExChangeClientEffectInfo(0, 0, 2);
	public static final ExChangeClientEffectInfo SEAL_OF_SHILLEN_ORFEN = new ExChangeClientEffectInfo(0, 0, 3);
	public static final ExChangeClientEffectInfo SEAL_OF_SHILLEN_QUEEN_ANT = new ExChangeClientEffectInfo(0, 0, 4);
	public static final ExChangeClientEffectInfo SEAL_OF_SHILLEN_ZAKEN = new ExChangeClientEffectInfo(0, 0, 5);
	private final int _type;
	private final int _key;
	private final int _value;

	public ExChangeClientEffectInfo(int type, int key, int value)
	{
		this._type = type;
		this._key = key;
		this._value = value;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CLIENT_EFFECT_INFO.writeId(this, buffer);
		buffer.writeInt(this._type);
		buffer.writeInt(this._key);
		buffer.writeInt(this._value);
	}
}
