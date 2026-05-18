package net.sf.l2jdev.loginserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.loginserver.network.LoginClient;

public class Init extends LoginServerPacket {
   private final int _sessionId;
   private final byte[] _publicKey;
   private final byte[] _blowfishKey;

   public Init(LoginClient client) {
      this(client.getScrambledModulus(), client.getBlowfishKey(), client.getSessionId());
   }

   public Init(byte[] publickey, byte[] blowfishkey, int sessionId) {
      this._sessionId = sessionId;
      this._publicKey = publickey;
      this._blowfishKey = blowfishkey;
   }

   @Override
   protected void writeImpl(LoginClient client, WritableBuffer buffer) {
      buffer.writeByte(0);
      buffer.writeInt(this._sessionId);
      buffer.writeInt(50721);
      buffer.writeBytes(this._publicKey);
      buffer.writeInt(702387534);
      buffer.writeInt(2009308412);
      buffer.writeInt(-1750223328);
      buffer.writeInt(129884407);
      buffer.writeBytes(this._blowfishKey);
      buffer.writeByte(0);
   }
}
