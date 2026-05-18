package net.sf.l2jdev.commons.network;

import java.nio.ByteBuffer;

import net.sf.l2jdev.commons.network.internal.SinglePacketBuffer;

public interface ReadableBuffer extends Buffer
{
	char readChar();

	byte readByte();

	byte[] readBytes(int var1);

	void readBytes(byte[] var1);

	void readBytes(byte[] var1, int var2, int var3);

	short readShort();

	int readInt();

	long readLong();

	float readFloat();

	double readDouble();

	int remaining();

	static ReadableBuffer of(ByteBuffer buffer)
	{
		return new SinglePacketBuffer(buffer);
	}
}
