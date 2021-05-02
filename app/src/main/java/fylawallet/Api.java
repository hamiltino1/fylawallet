package fylawallet;

//harmonyj
import one.harmony.cmd.Blockchain;
import one.harmony.common.Config;
import one.harmony.cmd.Keys;
import one.harmony.transaction.Handler;
import one.harmony.transaction.ChainID;
import one.harmony.cmd.Balance;
import one.harmony.cmd.Transfer;
import one.harmony.account.HistoryParams;

//web3j
import org.web3j.protocol.core.methods.response.TransactionReceipt;

//java
import java.util.Map;
import java.util.List;
import java.lang.StringBuilder;
import java.security.SecureRandom;
import java.nio.charset.Charset;
import java.util.Map;
import java.math.BigInteger;

//sql2o
import org.sql2o.*;
import org.sql2o.Sql2o;

//fylawallet
import static fylawallet.DbMethods.*;
import static fylawallet.DbMethods.toMap;
import fylawallet.MyGasProvider;

//BIP39
import io.github.novacrypto.bip39.MnemonicGenerator;
import io.github.novacrypto.bip39.Words;
import io.github.novacrypto.bip39.wordlists.English;


//sparkjava
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.halt;
import spark.Spark.*;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.ResponseTransformer;

//okhttp
import okhttp3.OkHttpClient;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Request.Builder;

//fylawallet
import static fylawallet.App.*;
import static fylawallet.JsonUtil.json;

////apache 
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.commons.lang3.exception.ExceptionUtils; 

//jackson
import com.fasterxml.jackson.databind.ObjectMapper; 

//json object
import org.json.JSONObject;

//gson
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.Gson;
import com.google.gson.JsonArray;


public class Api {
    

    public Api() {

        /**
         * Create account.
         **/
        post("/api/createAccount", (request, response) -> {
            try {
                //get params
                response.type("application/json");
                List<NameValuePair> pairs = URLEncodedUtils.parse(request.body(), Charset.defaultCharset());
                Map<String, String> params = toMap(pairs);
                String account = params.get("account");
                String password = params.get("password");
                //seed generator
                StringBuilder sb = new StringBuilder();
                byte[] entropy = new byte[Words.TWENTY_FOUR.byteLength()];
                new SecureRandom().nextBytes(entropy);
                new MnemonicGenerator(English.INSTANCE)
                    .createMnemonic(entropy, sb::append);
                String seed = sb.toString();
                System.out.println(seed);
                //purge account table
                DbMethods.deleteAccounts();
                //createAccount
                //seed = "bulk link solve gloom region hard bonus vessel assist riot rotate knee public gravity eager detect cricket endorse example spice bread wrong resist rocket";
                oneAddress = Keys.addKey(account, password, seed);
                accountName = account;
                passphrase = password;
                //System.out.println(oneAddress);
                //MyGasProvider contractGasProvider = new MyGasProvider(new BigInteger("1"), new BigInteger("6751900")); 
                //Load contract
                String contractAddress = "0x78f3092515647B8550Bd374c9894C2dB64829d34";
                contract = ERC20.load(contractAddress, contractGasProvider);
                //set handler
                //System.out.println(Keys.listAccounts());
                //password = "harmonypass";
                //String oneAddress = "one1due3ch59qj0x0agv4rfual2tnqsnr87fxk7gke";
                node = "https://api.s0.t.hmny.io";
                handler = new Handler(oneAddress, password, node, ChainID.MAINNET);
                System.out.println("got past handler");
                contract.setHandler(handler);
                String accountInfo  = Keys.exportKeyStoreFromAddress(oneAddress, password);
                System.out.println("got past key export");
                //one2 to jsonobject
                ObjectMapper mapper = new ObjectMapper();
                Map<String,Object> map = mapper.readValue(accountInfo, Map.class);
                ethFormattedAddress = "0x" + map.get("address"); 
                //Get private key of address
                String privateKey  = Keys.exportPrivateKeyFromAddress(oneAddress, password);
                System.out.println(privateKey);
                //insert into db
                DbMethods.insertAccount(account, password, seed, oneAddress, ethFormattedAddress, privateKey);
                return true;
            }
            catch(Exception e) {
                e.printStackTrace();
                String errorMsg = "Error: " + e.getMessage();;
                return halt(500, errorMsg);
            }
        }, json());
        /**
         * Login.
         **/
        post("/api/login", (request, response) -> {
            try {
                //get params
                response.type("application/json");
                List<NameValuePair> pairs = URLEncodedUtils.parse(request.body(), Charset.defaultCharset());
                Map<String, String> params = toMap(pairs);
                passphrase = params.get("password");
                //retreive account object
                List<AccountObject> accountList = DbMethods.accountInfo();
                AccountObject accountObject = accountList.get(0);
                oneAddress = accountObject.getAddress();
                ethFormattedAddress = accountObject.getEthFormat();
                System.out.println(oneAddress);
                System.out.println(passphrase);
                System.out.println(node);
                System.out.println(oneAddress);
                handler = new Handler(oneAddress, passphrase, node, ChainID.MAINNET);
                accountName = accountObject.getAccount();
                return true;
            }
            catch(Exception e) {
                e.printStackTrace();
                String errorMsg = "Error: " + e.getMessage();;
                return halt(500, errorMsg);
            }
        }, json());

        //Get balance.
        post("/api/balance", (request, response) -> {
            BigInteger balance = null;
            try {
                //get params
                response.type("application/json");
                List<NameValuePair> pairs = URLEncodedUtils.parse(request.body(), Charset.defaultCharset());
                Map<String, String> params = toMap(pairs);
                String coin = params.get("coin");
                System.out.println(coin);

                if(coin.equals("FYLA")) {
                    balance = contract.balanceOff(ethFormattedAddress).send();
                }
                if(coin.equals("ONE")) {
                    String balanceString = Balance.check(oneAddress);
                    ObjectMapper mapper = new ObjectMapper();
                    List<BalanceObject> balanceObject = mapper.readValue(String.valueOf(balanceString), List.class);
                    return balanceObject;
                }
            }
            catch(Exception e) {
                e.printStackTrace();
                String errorMsg = "Error: " + e.getMessage();;
                return halt(500, errorMsg);
            }
            return balance;
        }, json());
        //Transfer from.
        post("/api/transfer", (request, response) -> {
            try {
                //get params
                response.type("application/json");
                List<NameValuePair> pairs = URLEncodedUtils.parse(request.body(), Charset.defaultCharset());
                Map<String, String> params = toMap(pairs);
                String coin = params.get("coin");
                String from = params.get("from");
                String amount = params.get("amount");
                String to = params.get("to");
                TransactionReceipt tx = null;
                if(coin.equals("FYLA")) {
                    int i = Integer.parseInt(amount);  
                    i = i * 10;
                    amount = String.valueOf(i);
                    BigInteger bigAmount  = new BigInteger(params.get(amount));
                    bigAmount = bigAmount.pow(18);
                    tx = contract.transferFrom(oneAddress, to, bigAmount).send();
                    return tx;
                }
                if(coin.equals("ONE")) {
                    boolean dryRun = false;
                    int waitToConfirmTime = 0;
                    Transfer t = new Transfer(ethFormattedAddress, to, amount);
                    passphrase = "harmonypass";
                    t.prepare(passphrase); // prepare transfer locally, before connecting to the network
                    String txHash = t.execute(ChainID.MAINNET, dryRun, waitToConfirmTime); // needs connection to the network
                    return txHash;
                }
                return null;
            }
            catch(Exception e) {
                e.printStackTrace();
                String errorMsg = "Error: " + e.getMessage();;
                return halt(500, errorMsg);
            }
        }, json());

        post("/api/accountInfo", (request, response) -> {
            try {
                List<AccountObject> accountInfo = DbMethods.accountInfo();
                return accountInfo;
            }
            catch(Exception e) {
                e.printStackTrace();
                String errorMsg = "Error: " + e.getMessage();;
                return halt(500, errorMsg);
            }
        }, json());
        post("/api/onehistory", (request, response) -> {
            try {
                response.type("application/json");
                List<NameValuePair> pairs = URLEncodedUtils.parse(request.body(), Charset.defaultCharset());

                Map<String, String> params = toMap(pairs);
                String oneAddress = params.get("oneAddress");

                HistoryParams historyParams = new HistoryParams(oneAddress);
                String history = Blockchain.getAccountTransactions(historyParams);

                Gson gson = new Gson();
                String data=history;
                JsonParser jsonParser = new JsonParser();
                JsonArray jsonArray = (JsonArray) jsonParser.parse(data);
                return jsonArray;
            }
            catch(Exception e) {
                e.printStackTrace();
                String errorMsg = "Error: " + e.getMessage();;
                return halt(500, errorMsg);
            }
        }, json());

        post("/api/importseed", (request, response) -> {
            try {
                response.type("application/json");
                List<NameValuePair> pairs = URLEncodedUtils.parse(request.body(), Charset.defaultCharset());

                Map<String, String> params = toMap(pairs);
                String account = params.get("account");
                String password = params.get("password");
                String seed = params.get("seed");

                DbMethods.deleteAccounts();
                oneAddress = Keys.addKey(account, password, seed);
                DbMethods.deleteAccounts();

                //get ethFormattedAddress and privateKey
                String accountInfo  = Keys.exportKeyStoreFromAddress(oneAddress, password);
                ObjectMapper mapper1 = new ObjectMapper();
                Map<String,Object> map = mapper1.readValue(accountInfo, Map.class);
                ethFormattedAddress = "0x" + map.get("address"); 
                //get private key
                String privateKey  = Keys.exportPrivateKeyFromAddress(oneAddress, password);
                //insert into DB.
                DbMethods.insertAccount(account, password, seed, oneAddress, ethFormattedAddress, privateKey);
                return oneAddress;
            }
            catch(Exception e) {
                e.printStackTrace();
                String errorMsg = "Error: " + e.getMessage();;
                return halt(500, errorMsg);
            }
        }, json());

        post("/api/importprivatekey", (request, response) -> {
            try {
                //get params
                response.type("application/json");
                List<NameValuePair> pairs = URLEncodedUtils.parse(request.body(), Charset.defaultCharset());
                Map<String, String> params = toMap(pairs);
                String accountName = params.get("accountName");
                String password = params.get("password");
                String privateKey = params.get("privateKey");
                //clean keystore
                Keys.cleanKeyStore();
                Keys.importPrivateKey(privateKey, accountName, password);
                Map<String, String> listAccounts = Keys.listAccounts();
                String oneAddress = listAccounts.get(accountName); 
                oneAddress = oneAddress.substring(0,oneAddress.lastIndexOf('.'));
                oneAddress = oneAddress.replace(".", "");
                System.out.println(oneAddress);
                //one2 to jsonobject
                String accountInfo  = Keys.exportKeyStoreFromAddress(oneAddress, password);
                ObjectMapper mapper1 = new ObjectMapper();
                Map<String,Object> map = mapper1.readValue(accountInfo, Map.class);
                ethFormattedAddress = "0x" + map.get("address"); 
                System.out.println(ethFormattedAddress);
                passphrase = password;
                String contractAddress = "0x78f3092515647B8550Bd374c9894C2dB64829d34";
                contract = ERC20.load(contractAddress, contractGasProvider);
                handler = new Handler(oneAddress, passphrase, node, ChainID.MAINNET);
                contract.setHandler(handler);

                //updateDB
                DbMethods.updatePrivateKey(privateKey, accountName, password, oneAddress, ethFormattedAddress);  
            }
            catch(Exception e) {
                e.printStackTrace();
                String errorMsg = "Error: " + e.getMessage();;
                return halt(500, errorMsg);
            }
            return true;
        }, json());
    }
}
