package net.sf.l2jdev.gameserver.network.loginserverpackets.game;

import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;

import net.sf.l2jdev.commons.network.base.BaseWritablePacket;

public final class BlowFishKey extends BaseWritablePacket
{
	private static final Logger LOGGER = Logger.getLogger(BlowFishKey.class.getName());
 

	public BlowFishKey(byte[] blowfishKey, RSAPublicKey publicKey)
	{
		if (blowfishKey == null || blowfishKey.length == 0)
		{
			throw new IllegalArgumentException("blowfishKey must not be null/empty");
		}
		else if (publicKey == null)
		{
			throw new IllegalArgumentException("publicKey must not be null");
		}
		else
		{
			if (blowfishKey.length < 16 || blowfishKey.length > 64)
			{
				LOGGER.warning("BlowFishKey: Unexpected Blowfish key length: " + blowfishKey.length + " bytes.");
			}

			try
			{
				Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
				rsaCipher.init(1, publicKey);
				byte[] encryptedKey = rsaCipher.doFinal(blowfishKey);
				this.writeByte(0);
				this.writeInt(encryptedKey.length);
				this.writeBytes(encryptedKey);
			}
			catch (GeneralSecurityException var5)
			{
				LOGGER.log(Level.SEVERE, "BlowFishKey: RSA encryption failed.", var5);
			}
		}
	}
}
