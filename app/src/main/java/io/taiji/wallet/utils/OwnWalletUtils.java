package io.taiji.wallet.utils;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.taiji.crypto.CipherException;
import com.networknt.taiji.crypto.ECKeyPair;
import com.networknt.taiji.crypto.Wallet;
import com.networknt.taiji.crypto.WalletFile;
import com.networknt.taiji.crypto.WalletUtils;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;

public class OwnWalletUtils extends WalletUtils {
    public static ObjectMapper objectMapper = new ObjectMapper();

    /*
    public static String generateFullNewWalletFile(String password, File destinationDirectory, String chainId)
            throws NoSuchAlgorithmException, NoSuchProviderException,
            InvalidAlgorithmParameterException, CipherException, IOException {
        return generateNewWalletFile(password, destinationDirectory, chainId,true);
    }

    public static String generateNewWalletFile(
            String password, File destinationDirectory, String chainId, boolean useFullScrypt)
            throws CipherException, IOException, InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {
        AddressGenerator generator = new AddressGenerator(chainId);
        ECKeyPair ecKeyPair = generator.generate();
        KeyPair encryptingKeyPair = Keys.createCipherKeyPair();
        return generateWalletFile(password, ecKeyPair, encryptingKeyPair, destinationDirectory, useFullScrypt);
    }
    */

    public static String generateWalletFile(
            String password, ECKeyPair ecKeyPair, KeyPair encryptingKeyPair, File destinationDirectory, boolean useFullScrypt)
            throws CipherException, IOException {
        Log.i("TAG", "Gererating wallet file");
        WalletFile walletFile = Wallet.createStandard(password, ecKeyPair, encryptingKeyPair);
        String fileName = getWalletFileName(walletFile);
        Log.i("TAG", "Wallet fileName " + fileName + " in destination " + destinationDirectory.getAbsolutePath());
        File destination = new File(destinationDirectory, fileName);
        objectMapper.writeValue(destination, walletFile);
        return fileName;
    }

    private static String getWalletFileName(WalletFile walletFile) {
        return walletFile.getAddress();
    }

}