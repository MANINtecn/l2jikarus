package net.sf.l2jdev.loginserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.loginserver.enums.LoginFailReason;
import net.sf.l2jdev.loginserver.network.LoginClient;

public class LoginFail extends LoginServerPacket {
   private final LoginFailReason _reason;

   public LoginFail(LoginFailReason reason) {
      this._reason = reason;
   }

   @Override
   protected void writeImpl(LoginClient client, WritableBuffer buffer) {
      buffer.writeByte(1);
      buffer.writeByte(this._reason.getCode());
   }
}
