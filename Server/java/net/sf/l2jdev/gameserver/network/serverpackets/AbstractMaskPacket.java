package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.gameserver.model.interfaces.IUpdateTypeComponent;

public abstract class AbstractMaskPacket<T extends IUpdateTypeComponent> extends ServerPacket
{
	protected static final byte[] DEFAULT_FLAG_ARRAY = new byte[]
	{
		-128,
		64,
		32,
		16,
		8,
		4,
		2,
		1
	};

	protected abstract byte[] getMasks();

	protected void onNewMaskAdded(T component)
	{
	}

	@SafeVarargs
	public final void addComponentType(T... updateComponents)
	{
		for (T component : updateComponents)
		{
			if (!this.containsMask(component))
			{
				this.addMask(component.getMask());
				this.onNewMaskAdded(component);
			}
		}
	}

	protected void addMask(int mask)
	{
		byte[] var10000 = this.getMasks();
		var10000[mask >> 3] = (byte) (var10000[mask >> 3] | DEFAULT_FLAG_ARRAY[mask & 7]);
	}

	public boolean containsMask(T component)
	{
		return this.containsMask(component.getMask());
	}

	public boolean containsMask(int mask)
	{
		return (this.getMasks()[mask >> 3] & DEFAULT_FLAG_ARRAY[mask & 7]) != 0;
	}

	public boolean containsMask(int masks, T type)
	{
		return (masks & type.getMask()) == type.getMask();
	}
}
