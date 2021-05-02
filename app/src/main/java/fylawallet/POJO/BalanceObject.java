package fylawallet;

import java.math.BigInteger;
import java.math.BigDecimal;

public class BalanceObject  {

    	private String shard;

    	private String amount;

    	public BalanceObject(String amount, String shard) {
            this.amount = amount;
            this.shard = shard;
            
	    }
    	public String getShard() {
        	return shard;
    	}
        public String getAmount() {
            return amount;
        }
               
}

