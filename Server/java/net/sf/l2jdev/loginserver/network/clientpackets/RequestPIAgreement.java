package net.sf.l2jdev.loginserver.network.clientpackets;

import net.sf.l2jdev.loginserver.network.serverpackets.PIAgreementAck;

public class RequestPIAgreement extends LoginClientPacket {
   private int _accountId;
   private int _status;

   @Override
   protected boolean readImpl() {
      this._accountId = this.readInt();
      this._status = this.readByte();
      return true;
   }

   @Override
   public void run() {
      this.getClient().sendPacket(new PIAgreementAck(this._accountId, this._status));
   }
}
