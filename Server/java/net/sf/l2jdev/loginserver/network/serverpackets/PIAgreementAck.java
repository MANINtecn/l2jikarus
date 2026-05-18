package net.sf.l2jdev.loginserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.loginserver.network.LoginClient;

public class PIAgreementAck extends LoginServerPacket {
   private final int _accountId;
   private final int _status;

   public PIAgreementAck(int accountId, int status) {
      this._accountId = accountId;
      this._status = status;
   }

   @Override
   protected void writeImpl(LoginClient client, WritableBuffer buffer) {
      buffer.writeByte(18);
      buffer.writeInt(this._accountId);
      buffer.writeByte(this._status);
   }
}
