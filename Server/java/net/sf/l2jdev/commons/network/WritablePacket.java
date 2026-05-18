package net.sf.l2jdev.commons.network;

import net.sf.l2jdev.commons.network.internal.ArrayPacketBuffer;
import net.sf.l2jdev.commons.network.internal.InternalWritableBuffer;

public abstract class WritablePacket<T extends Client<Connection<T>>>
{
	private volatile boolean _broadcast;
	private ArrayPacketBuffer _broadcastCacheBuffer;

	protected WritablePacket()
	{
	}

	public InternalWritableBuffer writeData(T client) throws Exception
	{
		return this._broadcast ? this.writeDataWithCache(client) : this.writeDataToBuffer(client);
	}

	private synchronized InternalWritableBuffer writeDataWithCache(T client) throws Exception
	{
		if (this._broadcastCacheBuffer != null)
		{
			return InternalWritableBuffer.dynamicOf(this._broadcastCacheBuffer, client.getResourcePool(), this.getClass());
		}
		InternalWritableBuffer buffer = this.writeDataToBuffer(client);
		if (buffer instanceof ArrayPacketBuffer)
		{
			this._broadcastCacheBuffer = (ArrayPacketBuffer) buffer;
			buffer = InternalWritableBuffer.dynamicOf(this._broadcastCacheBuffer, client.getResourcePool(), this.getClass());
		}

		return buffer;
	}

	private InternalWritableBuffer writeDataToBuffer(T client) throws Exception
	{
		InternalWritableBuffer buffer = this.choosePacketBuffer(client);
		buffer.position(2);
		if (this.write(client, buffer))
		{
			buffer.mark();
			return buffer;
		}
		buffer.releaseResources();
		throw new Exception();
	}

	private InternalWritableBuffer choosePacketBuffer(T client)
	{
		return this._broadcast ? InternalWritableBuffer.arrayBacked(client.getResourcePool(), this.getClass()) : InternalWritableBuffer.dynamicOf(client.getResourcePool(), this.getClass());
	}

	public void writeHeader(InternalWritableBuffer buffer, int header)
	{
		buffer.writeShort(0, (short) header);
	}

	public void sendInBroadcast()
	{
		this._broadcast = true;
	}

	public boolean canBeDropped(T client)
	{
		return false;
	}

	protected abstract boolean write(T var1, WritableBuffer var2);

	@Override
	public String toString()
	{
		return this.getClass().getSimpleName();
	}
}
