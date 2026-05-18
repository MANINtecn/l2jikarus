package net.sf.l2jdev.loginserver.network;

import java.io.IOException;

import net.sf.l2jdev.commons.network.Buffer;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.loginserver.crypt.NewCrypt;

public class LoginEncryption
{
	private static final byte[] STATIC_BLOWFISH_KEY = new byte[]
	{
		107,
		96,
		-53,
		91,
		-126,
		-50,
		-112,
		-79,
		-52,
		43,
		108,
		85,
		108,
		108,
		108,
		108
	};
	private static final NewCrypt STATIC_CRYPT = new NewCrypt(STATIC_BLOWFISH_KEY);
	private NewCrypt _sessionCrypt;
	private boolean _usingStaticKey = true;
	
	public void setKey(byte[] key)
	{
		this._sessionCrypt = new NewCrypt(key);
	}
	
	public boolean decrypt(Buffer data, int offset, int size) throws IOException
	{
		this._sessionCrypt.decrypt(data, offset, size);
		return NewCrypt.verifyChecksum(data, offset, size);
	}
	
	public int encryptedSize(int dataSize)
	{
		int headerSize = this._usingStaticKey ? 8 : 4;
		int sizeWithHeader = dataSize + headerSize;
		int remainder = sizeWithHeader % 8;
		sizeWithHeader += 8 - remainder;
		return sizeWithHeader + 8;
	}
	
	public boolean encrypt(Buffer data, int offset, int size) throws IOException
	{
		int packetSize = this.encryptedSize(size);
		int packetEndOffset = offset + packetSize;
		data.limit(packetEndOffset);
		if (this._usingStaticKey)
		{
			this.encryptWithStaticKey(data, offset, packetEndOffset);
			this._usingStaticKey = false;
		}
		else
		{
			this.encryptWithSessionKey(data, offset, packetEndOffset);
		}
		
		return true;
	}
	
	protected void encryptWithStaticKey(Buffer data, int offset, int packetEndOffset) throws IOException
	{
		NewCrypt.encXORPass(data, offset, packetEndOffset, Rnd.nextInt());
		STATIC_CRYPT.crypt(data, offset, packetEndOffset);
	}
	
	private void encryptWithSessionKey(Buffer data, int offset, int packetEndOffset) throws IOException
	{
		NewCrypt.appendChecksum(data, offset, packetEndOffset);
		this._sessionCrypt.crypt(data, offset, packetEndOffset);
	}
}
