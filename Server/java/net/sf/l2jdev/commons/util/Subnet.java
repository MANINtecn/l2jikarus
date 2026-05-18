package net.sf.l2jdev.commons.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public class Subnet
{
	private final byte[] _address;
	private final byte[] _subnetMask;
	private final boolean _v4;

	public Subnet(String input) throws UnknownHostException
	{
		String[] parts = input.split("/");
		InetAddress inetAddress = InetAddress.getByName(parts[0]);
		this._address = inetAddress.getAddress();
		this._v4 = this._address.length == 4;
		int prefixLength = this._v4 ? 32 : 128;
		if (parts.length > 1)
		{
			prefixLength = Integer.parseInt(parts[1]);
		}

		if (prefixLength >= 0 && prefixLength <= this._address.length * 8)
		{
			int fullBytes = prefixLength / 8;
			int remainingBits = prefixLength % 8;
			this._subnetMask = new byte[this._address.length];
			Arrays.fill(this._subnetMask, 0, fullBytes, (byte) -1);
			if (remainingBits > 0)
			{
				this._subnetMask[fullBytes] = (byte) (255 << 8 - remainingBits);
			}

			for (int i = 0; i < this._address.length; i++)
			{
				this._address[i] = (byte) (this._address[i] & this._subnetMask[i]);
			}
		}
		else
		{
			throw new IllegalArgumentException("Invalid prefix length: " + prefixLength);
		}
	}

	public boolean isInSubnet(byte[] address)
	{
		if (address.length != this._address.length)
		{
			if (!this._v4 || address.length != 16)
			{
				return false;
			}

			byte[] ipv4In6Prefix = new byte[]
			{
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				-1,
				-1
			};

			for (int i = 0; i < ipv4In6Prefix.length; i++)
			{
				if (address[i] != ipv4In6Prefix[i])
				{
					return false;
				}
			}

			address = Arrays.copyOfRange(address, 12, 16);
		}

		for (int ix = 0; ix < address.length; ix++)
		{
			if ((address[ix] & this._subnetMask[ix]) != this._address[ix])
			{
				return false;
			}
		}

		return true;
	}

	public byte[] getAddress()
	{
		return _address.clone();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		else if (obj instanceof Subnet)
		{
			return this.isInSubnet(((Subnet) obj)._address);
		}
		else
		{
			return obj instanceof InetAddress ? this.isInSubnet(((InetAddress) obj).getAddress()) : false;
		}
	}

	@Override
	public String toString()
	{
		int prefixLength = 0;

		for (byte b : this._subnetMask)
		{
			prefixLength += Integer.bitCount(b & 255);
		}

		try
		{
			return InetAddress.getByAddress(this._address).getHostAddress() + "/" + prefixLength;
		}
		catch (UnknownHostException var6)
		{
			return "Invalid address";
		}
	}
}
