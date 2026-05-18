package net.sf.l2jdev.loginserver.network.gameserverpackets;

import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;

import net.sf.l2jdev.commons.crypt.NewCrypt;
import net.sf.l2jdev.commons.network.base.BaseReadablePacket;
import net.sf.l2jdev.loginserver.GameServerThread;
import net.sf.l2jdev.loginserver.network.GameServerPacketHandler;

public class BlowFishKey extends BaseReadablePacket
{
	protected static final Logger LOGGER = Logger.getLogger(BlowFishKey.class.getName());
	
	public BlowFishKey(byte[] decrypt, GameServerThread server)
	{
		super(decrypt);
		this.readByte();
		int size = this.readInt();
		byte[] tempKey = this.readBytes(size);
		
		try
		{
			Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(2, server.getPrivateKey());
			byte[] tempDecryptKey = rsaCipher.doFinal(tempKey);
			int i = 0;
			int len = tempDecryptKey.length;
			
			while (i < len && tempDecryptKey[i] == 0)
			{
				i++;
			}
			
			byte[] key = new byte[len - i];
			System.arraycopy(tempDecryptKey, i, key, 0, len - i);
			server.setBlowFish(new NewCrypt(key));
			server.setLoginConnectionState(GameServerPacketHandler.GameServerState.BF_CONNECTED);
		}
		catch (GeneralSecurityException var10)
		{
			LOGGER.log(Level.SEVERE, "Error While decrypting blowfish key (RSA): " + var10.getMessage(), var10);
		}
	}
}
