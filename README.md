# fylawallet
downloadable wallet for FYLA and ONE.

Dependencies:
Java 8+
gradle

Run gradle assemble in the app directory and go to the build/libs folder and run the jar with java -jar *.jar

API's(POST)

/api/createAccount params - account, password


/api/login params - password


/api/balance params - coin(FYLA or ONE)


/api/transfer params - coin(FYLA or ONE), from, to, amount


/api/accountInfo - params - null


/api/onehistory - params - oneAddress


/api/importseed - params - account, password, seed


/api/importprivatekey - params - accountName, password, privateKey
