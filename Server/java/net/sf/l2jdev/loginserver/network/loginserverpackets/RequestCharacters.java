package net.sf.l2jdev.loginserver.network.loginserverpackets;

import net.sf.l2jdev.commons.network.base.BaseWritablePacket;

public class RequestCharacters extends BaseWritablePacket {
   public RequestCharacters(String account) {
      this.writeByte(5);
      this.writeString(account);
   }
}
