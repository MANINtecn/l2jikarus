package net.sf.l2jdev.loginserver.network.loginserverpackets;

import net.sf.l2jdev.commons.network.base.BaseWritablePacket;

public class ChangePasswordResponse extends BaseWritablePacket {
   public ChangePasswordResponse(String characterName, String msgToSend) {
      this.writeByte(6);
      this.writeString(characterName);
      this.writeString(msgToSend);
   }
}
