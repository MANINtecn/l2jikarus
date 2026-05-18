package net.sf.l2jdev.gameserver.network;

import net.sf.l2jdev.commons.network.Buffer;

public class Encryption
{
	 
	private final byte[] _inKey = new byte[16];
	private final byte[] _outKey = new byte[16];
	private boolean _isEnabled;

	public void setKey(byte[] key)
	{
		if (key != null && key.length >= 16)
		{
			System.arraycopy(key, 0, this._inKey, 0, 16);
			System.arraycopy(key, 0, this._outKey, 0, 16);
		}
		else
		{
			throw new IllegalArgumentException("Encryption key must be at least 16 bytes.");
		}
	}

	public void encrypt(Buffer data, int offset, int size)
	{
		if (!this._isEnabled)
		{
			this._isEnabled = true;
		}
		else if (size > 0)
		{
			int prev = 0;

			for (int i = 0; i < size; i++)
			{
				int raw = Byte.toUnsignedInt(data.readByte(offset + i));
				prev ^= raw ^ this._outKey[i & 15] & 255;
				data.writeByte(offset + i, (byte) prev);
			}

			int old = this._outKey[0] & 255;
			old |= (this._outKey[1] & 255) << 8;
			old |= (this._outKey[2] & 255) << 16;
			old |= (this._outKey[3] & 255) << 24;
			old += size;
			this._outKey[0] = (byte) (old & 0xFF);
			this._outKey[1] = (byte) (old >> 8 & 0xFF);
			this._outKey[2] = (byte) (old >> 16 & 0xFF);
			this._outKey[3] = (byte) (old >> 24 & 0xFF);
		}
	}

	public void decrypt(Buffer data, int offset, int size)
	{
		if (this._isEnabled)
		{
			if (size > 0)
			{
				int last = 0;

				for (int i = 0; i < size; i++)
				{
					int enc = Byte.toUnsignedInt(data.readByte(offset + i));
					data.writeByte(offset + i, (byte) (enc ^ this._inKey[i & 15] & 255 ^ last));
					last = enc;
				}

				int old = this._inKey[0] & 255;
				old |= (this._inKey[1] & 255) << 8;
				old |= (this._inKey[2] & 255) << 16;
				old |= (this._inKey[3] & 255) << 24;
				old += size;
				this._inKey[0] = (byte) (old & 0xFF);
				this._inKey[1] = (byte) (old >> 8 & 0xFF);
				this._inKey[2] = (byte) (old >> 16 & 0xFF);
				this._inKey[3] = (byte) (old >> 24 & 0xFF);
			}
		}
	}
}
