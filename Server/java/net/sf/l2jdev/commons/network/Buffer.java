package net.sf.l2jdev.commons.network;

public interface Buffer
{
	byte readByte(int var1);

	void writeByte(int var1, byte var2);

	short readShort(int var1);

	void writeShort(int var1, short var2);

	int readInt(int var1);

	void writeInt(int var1, int var2);

	int limit();

	void limit(int var1);
}
