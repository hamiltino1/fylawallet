package fylawallet;

//java
import java.util.*;
import java.util.Map;

//sql2o
import org.sql2o.*;

//fylawallet
import static fylawallet.App.*;

//apache
import org.apache.http.NameValuePair;

//Contract
import one.harmony.cmd.Contract;

public class DbMethods {

    //returns all users in a list
	public static List<AccountObject> accountInfo(){
		String sql =
		"SELECT address, eth_format, account, seed, privateKey " +
		"FROM accounts";

	    	try(Connection con = sql2o.open()) {
			    return con.createQuery(sql).executeAndFetch(AccountObject.class);
	        }
	}
    public static void deleteAccounts(){
		String sql = "DELETE FROM accounts";
	    	try(Connection con = sql2o.open()) {
            con.createQuery(sql)
				.executeUpdate();
	    }
	}
    //Inserts new user into db
	public static boolean insertAccount(String account, String password, String seed, String address, String eth_format, String privateKey) {
            final String insertQuery =
            "INSERT INTO accounts (account, address, eth_format, seed, password, privateKey) " +
            "VALUES (:account, :address, :eth_format, :seed, :password, :privateKey)";

            Connection con = sql2o.beginTransaction(java.sql.Connection.TRANSACTION_READ_UNCOMMITTED);  
                con.createQuery(insertQuery)
                    .addParameter("account", account)
                    .addParameter("privateKey", privateKey)
                    .addParameter("address", address)
                    .addParameter("eth_format", eth_format)
                    .addParameter("password", password)
                    .addParameter("seed", seed)
                    .executeUpdate();
                con.commit();
                System.out.println("commited");

            return true;
	}	
    //Inserts new user into db
	public static boolean updatePrivateKey(String privateKey, String accountName, String password, String oneAddress, String eth_format) {
            final String insertQuery = "UPDATE accounts set account = :accountName, password = :password, privateKey = :privateKey, address = :oneAddress, eth_format = :eth_format";

            Connection con = sql2o.beginTransaction(java.sql.Connection.TRANSACTION_READ_UNCOMMITTED);  
                con.createQuery(insertQuery)
                    .addParameter("accountName", accountName)
                    .addParameter("privateKey", privateKey)
                    .addParameter("password", password)
                    .addParameter("oneAddress", oneAddress)
                    .addParameter("eth_format", eth_format)
                    .executeUpdate();
                con.commit();
                System.out.println("commited");
            return true;
	}	

    public static Map<String, String> toMap(List<NameValuePair> pairs) {
    Map<String, String> map = new HashMap<>();
    for(int i=0; i<pairs.size(); i++){
        NameValuePair pair = pairs.get(i);
        map.put(pair.getName(), pair.getValue());
    }
    return map;
	}
}
