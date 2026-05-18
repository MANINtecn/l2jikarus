package net.sf.l2jdev.loginserver.network;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;

public class ScrambledKeyPair
{
	private final KeyPair _pair;
	private final byte[] _scrambledModulus;
	
	public ScrambledKeyPair(KeyPair pair)
	{
		this._pair = pair;
		this._scrambledModulus = this.scrambleModulus(((RSAPublicKey) this._pair.getPublic()).getModulus());
	}
	
	protected byte[] scrambleModulus(BigInteger modulus)
	{
		byte[] scrambledMod = modulus.toByteArray();
		if (scrambledMod.length == 129 && scrambledMod[0] == 0)
		{
			byte[] temp = new byte[128];
			System.arraycopy(scrambledMod, 1, temp, 0, 128);
			scrambledMod = temp;
		}
		
		for (int i = 0; i < 4; i++)
		{
			byte temp = scrambledMod[0 + i];
			scrambledMod[0 + i] = scrambledMod[77 + i];
			scrambledMod[77 + i] = temp;
		}
		
		for (int i = 0; i < 64; i++)
		{
			scrambledMod[i] ^= scrambledMod[64 + i];
		}
		
		for (int i = 0; i < 4; i++)
		{
			scrambledMod[13 + i] = (byte) (scrambledMod[13 + i] ^ scrambledMod[52 + i]);
		}
		
		for (int i = 0; i < 64; i++)
		{
			scrambledMod[64 + i] = (byte) (scrambledMod[64 + i] ^ scrambledMod[i]);
		}
		
		return scrambledMod;
	}
	
	public byte[] getScrambledModulus()
	{
		return this._scrambledModulus;
	}
	
	public Key getPrivateKey()
	{
		return this._pair.getPrivate();
	}
	
	public Key getPublicKey()
	{
		return this._pair.getPublic();
	}
}
