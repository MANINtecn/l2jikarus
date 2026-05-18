package net.sf.l2jdev.commons.network;

@FunctionalInterface
public interface PacketHandler<T extends Client<Connection<T>>>
{
	ReadablePacket<T> handlePacket(ReadableBuffer var1, T var2);
}
