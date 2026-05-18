package net.sf.l2jdev.loginserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.loginserver.enums.PlayFailReason;
import net.sf.l2jdev.loginserver.network.LoginClient;

public class PlayFail extends LoginServerPacket {
   private final PlayFailReason _reason;

   public PlayFail(PlayFailReason reason) {
      this._reason = reason;
   }

   @Override
   protected void writeImpl(LoginClient client, WritableBuffer buffer) {
      buffer.writeByte(6);
      buffer.writeByte(this._reason.getCode());
   }
}
