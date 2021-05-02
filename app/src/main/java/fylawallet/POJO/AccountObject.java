package fylawallet;

import java.math.BigInteger;
import java.math.BigDecimal;

public class AccountObject  {

    	private String account;

    	private String seed;

    	private String address;

    	private String eth_format;

    	private String privateKey;

    	public AccountObject(String address, String seed, String account, String eth_format, String privateKey) {
            this.address = address;
            this.privateKey = privateKey;
            this.eth_format = eth_format;
            this.account = account;
            this.seed = seed;
	    }
    	public String getSeed() {
        	return seed;
    	}
        public String getEthFormat() {
        	return eth_format;
    	}

        public String getAddress() {
            return address;
        }
            
        public String getAccount() {
            return account;
        }
        public String getPrivateKey() {
            return privateKey;
        }
}

