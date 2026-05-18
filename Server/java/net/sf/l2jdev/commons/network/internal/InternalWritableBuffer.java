package net.sf.l2jdev.commons.network.internal;

import java.nio.ByteBuffer;

import net.sf.l2jdev.commons.network.ResourcePool;
import net.sf.l2jdev.commons.network.WritableBuffer;

public abstract class InternalWritableBuffer extends WritableBuffer
{
	public abstract int position();

	public abstract void position(int var1);

	public abstract void mark();

	public abstract ByteBuffer[] toByteBuffers();

	public abstract void releaseResources();

	public static InternalWritableBuffer dynamicOf(ArrayPacketBuffer buffer, ResourcePool resourcePool, Class<?> packetClass)
	{
		DynamicPacketBuffer copy = new DynamicPacketBuffer(buffer.toByteBuffer(), resourcePool, packetClass);
		copy.limit(buffer.limit());
		return copy;
	}

	public static InternalWritableBuffer dynamicOf(ResourcePool resourcePool, Class<?> packetClass)
	{
		return new DynamicPacketBuffer(resourcePool, packetClass);
	}

	public static InternalWritableBuffer arrayBacked(ResourcePool resourcePool, Class<?> packetClass)
	{
		return new ArrayPacketBuffer(resourcePool, packetClass);
	}
}
