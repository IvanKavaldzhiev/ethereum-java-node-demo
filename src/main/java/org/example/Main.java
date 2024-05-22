package org.example;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Numeric;

public class Main {
    public static void main(String[] args)
            throws Exception {
        Web3j web3j = Web3j.build(new HttpService("http://localhost:8544"));

        Credentials credentials = Credentials.create("8f2a55949038a9610f50fb23b5883af3b4ecb3c3bb792cbcefbd1542c692be63");

        JPrimeExample jPrimeExample = JPrimeExample.deploy(
                web3j, credentials, new DefaultGasProvider()).send();
        Optional<TransactionReceipt> transactionReceipt = jPrimeExample.getTransactionReceipt();  // constructor params

        String contractAddress = "0x0000000000000000000000000000000000000000";
        if(transactionReceipt.isPresent()) {
            contractAddress = transactionReceipt.get().getContractAddress();
        }

        System.out.println("Contract deploy transaction hash: " + transactionReceipt.get().getTransactionHash());

        Function function = new Function(
                "multiplySimpleNumbers",  // Function name we're calling
                Collections.emptyList(),  // Parameters to pass as Solidity Types
                Collections.emptyList()); // Function return types

        String encodedFunction = FunctionEncoder.encode(function);

        final var transactionCount = web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameter.valueOf("latest")).sendAsync().get();

        System.out.println("Transaction count: " + transactionCount.getTransactionCount());

        RawTransaction rawTransaction = RawTransaction.createTransaction(
                transactionCount.getTransactionCount(), BigInteger.valueOf(10000000L), BigInteger.valueOf(3000000L), contractAddress, BigInteger.ZERO, encodedFunction);

        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction,
                credentials);

        String hexValue = Numeric.toHexString(signedMessage);
        org.web3j.protocol.core.methods.response.EthSendTransaction transactionResponse =
                web3j.ethSendRawTransaction(hexValue).sendAsync().get();

        TimeUnit.SECONDS.sleep(40);

        String transactionHash = transactionResponse.getTransactionHash();

        System.out.println("Contract call transaction hash: " + transactionHash);

        final BigInteger callResult = jPrimeExample.multiplySimpleNumbers().send();

        System.out.println("Smart contract function call result: " + callResult);
    }
}