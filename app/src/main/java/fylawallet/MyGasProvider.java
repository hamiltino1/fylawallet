package fylawallet;

import org.web3j.tx.gas.StaticGasProvider;
import java.math.BigInteger;

public class MyGasProvider extends StaticGasProvider {
	public MyGasProvider(BigInteger gasPrice, BigInteger gasLimit) {
		super(gasPrice, gasLimit);
	}
}
