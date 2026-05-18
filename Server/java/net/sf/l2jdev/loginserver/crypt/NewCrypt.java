package net.sf.l2jdev.loginserver.crypt;

import java.io.IOException;

import net.sf.l2jdev.commons.network.Buffer;

public class NewCrypt
{
	private final BlowfishEngine _encryptionEngine = new BlowfishEngine();
	private final BlowfishEngine _decryptionEngine;
	
	public NewCrypt(byte[] blowfishKey)
	{
		this._encryptionEngine.init(true, blowfishKey);
		this._decryptionEngine = new BlowfishEngine();
		this._decryptionEngine.init(false, blowfishKey);
	}
	
	public NewCrypt(String key)
	{
		this(key.getBytes());
	}
	
	public static boolean verifyChecksum(Buffer data, int offset, int size)
	{
		if ((size & 3) == 0 && size > 4)
		{
			long checksum = 0L;
			int count = size - 4;
			
			int i;
			for (i = offset; i < count; i += 4)
			{
				checksum ^= data.readInt(i);
			}
			
			return data.readInt(i) == checksum;
		}
		return false;
	}
	
	public static void appendChecksum(Buffer data, int offset, int size)
	{
		int checksum = 0;
		int count = size - 4;
		
		int i;
		for (i = offset; i < count; i += 4)
		{
			checksum ^= data.readInt(i);
		}
		
		data.writeInt(i, checksum);
	}
	
	public static void encXORPass(Buffer rawData, int offset, int size, int xorKey)
	{
		int stopPosition = size - 8;
		int currentPosition = 4 + offset;
		
		int progressiveKey;
		for (progressiveKey = xorKey; currentPosition < stopPosition; currentPosition += 4)
		{
			int dataValue = rawData.readInt(currentPosition);
			progressiveKey += dataValue;
			dataValue ^= progressiveKey;
			rawData.writeInt(currentPosition, dataValue);
		}
		
		rawData.writeInt(currentPosition, progressiveKey);
	}
	
	public synchronized void decrypt(Buffer rawData, int offset, int size) throws IOException
	{
		int blockSize = this._decryptionEngine.getBlockSize();
		int blockCount = size / blockSize;
		
		for (int i = 0; i < blockCount; i++)
		{
			this._decryptionEngine.processBlock(rawData, offset + i * blockSize);
		}
	}
	
	public synchronized void crypt(Buffer rawData, int offset, int size) throws IOException
	{
		int blockSize = this._encryptionEngine.getBlockSize();
		int blockCount = size / blockSize;
		
		for (int i = 0; i < blockCount; i++)
		{
			this._encryptionEngine.processBlock(rawData, offset + i * blockSize);
		}
	}
}
