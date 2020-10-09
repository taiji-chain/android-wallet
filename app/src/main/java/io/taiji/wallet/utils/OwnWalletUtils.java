package io.taiji.wallet.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.networknt.config.JsonMapper;
import com.networknt.taiji.crypto.AddressGenerator;
import com.networknt.taiji.crypto.CipherException;
import com.networknt.taiji.crypto.ECKeyPair;
import com.networknt.taiji.crypto.Keys;
import com.networknt.taiji.crypto.Wallet;
import com.networknt.taiji.crypto.WalletFile;
import com.networknt.taiji.crypto.WalletUtils;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class OwnWalletUtils extends WalletUtils {

    // OVERRIDING THOSE METHODS BECAUSE OF CUSTOM WALLET NAMING (CUTING ALL THE TIMESTAMPTS FOR INTERNAL STORAGE)

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

    public static String generateWalletFile(
            String password, ECKeyPair ecKeyPair, KeyPair encryptingKeyPair, File destinationDirectory, boolean useFullScrypt)
            throws CipherException, IOException {

        WalletFile walletFile;
        walletFile = Wallet.createStandard(password, ecKeyPair, encryptingKeyPair);
        String fileName = getWalletFileName(walletFile);
        File destination = new File(destinationDirectory, fileName);
        JsonMapper.objectMapper.writeValue(destination, walletFile);
        return fileName;
    }

    private static String getWalletFileName(WalletFile walletFile) {
        return walletFile.getAddress();
    }

}