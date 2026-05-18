package net.sf.l2jdev.loginserver.network.clientpackets;

import net.sf.l2jdev.loginserver.config.LoginConfig;
import net.sf.l2jdev.loginserver.network.serverpackets.PIAgreementCheck;

public class RequestPIAgreementCheck extends LoginClientPacket {
   private int _accountId;

   @Override
   protected boolean readImpl() {
      this._accountId = this.readInt();
      byte[] padding0 = new byte[3];
      byte[] checksum = new byte[4];
      byte[] padding1 = new byte[12];
      this.readBytes(padding0);
      this.readBytes(checksum);
      this.readBytes(padding1);
      return true;
   }

   @Override
   public void run() {
      this.getClient().sendPacket(new PIAgreementCheck(this._accountId, LoginConfig.SHOW_PI_AGREEMENT ? 1 : 0));
   }
}
