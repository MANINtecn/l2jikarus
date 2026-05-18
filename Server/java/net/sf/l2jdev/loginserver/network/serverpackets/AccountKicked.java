package net.sf.l2jdev.loginserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.loginserver.enums.AccountKickedReason;
import net.sf.l2jdev.loginserver.network.LoginClient;

public class AccountKicked extends LoginServerPacket {
   private final AccountKickedReason _reason;

   public AccountKicked(AccountKickedReason reason) {
      this._reason = reason;
   }

   @Override
   protected void writeImpl(LoginClient client, WritableBuffer buffer) {
      buffer.writeByte(2);
      buffer.writeInt(this._reason.getCode());
   }
}
