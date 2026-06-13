package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

/**
 * Cópia enxuta do ExServerPrimitive só pro telegraph do mod No-Target: desenha apenas LINHAS e
 * zera os dois campos (antes 65535/65535) que parecem gerar o ícone-placeholder do ponto de origem.
 * Mantém um nome fixo pra o cliente substituir o desenho anterior (sem rastro).
 */
public class NoTargetConePacket extends ServerPacket
{
	private final String _name;
	private final int _x;
	private final int _y;
	private final int _z;
	private final List<int[]> _lines = new ArrayList<>(); // {color, x1, y1, z1, x2, y2, z2}

	public NoTargetConePacket(String name, int x, int y, int z)
	{
		this._name = name;
		this._x = x;
		this._y = y;
		this._z = z;
	}

	public void addLine(int color, int x1, int y1, int z1, int x2, int y2, int z2)
	{
		this._lines.add(new int[]
		{
			color,
			x1,
			y1,
			z1,
			x2,
			y2,
			z2
		});
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SERVER_PRIMITIVE.writeId(this, buffer);
		buffer.writeString(this._name);
		buffer.writeInt(this._x);
		buffer.writeInt(this._y);
		buffer.writeInt(this._z);
		buffer.writeInt(65535); // bounds de renderizacao (necessario pra desenhar)
		buffer.writeInt(65535);
		buffer.writeInt(this._lines.size());

		for (int[] line : this._lines)
		{
			buffer.writeByte(2);
			buffer.writeString(""); // sem nome na linha
			final int color = line[0];
			buffer.writeInt(color >> 16 & 0xFF);
			buffer.writeInt(color >> 8 & 0xFF);
			buffer.writeInt(color & 0xFF);
			buffer.writeInt(0); // isNameColored = false
			buffer.writeInt(line[1]);
			buffer.writeInt(line[2]);
			buffer.writeInt(line[3]);
			buffer.writeInt(line[4]);
			buffer.writeInt(line[5]);
			buffer.writeInt(line[6]);
		}
	}
}
