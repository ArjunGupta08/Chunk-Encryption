package org.example;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {

    public static void main(String[] args) {

        // Create the large input file first ------------

        String originalFile = "large_input.txt";

        long fileSize = (long) 1024 * 1024 * 1024; // 1 GB
        createLargeFile(originalFile, fileSize);

    }

    // Reading data from a file, encrypting it, and writing the encrypted data to another file.
    public static void encryptFile(String inputFile, String outputFile, String base64Key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
        byte[] iv = new byte[16]; // AES block size is 16 bytes
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        try (FileInputStream in = new FileInputStream(inputFile);
             FileOutputStream out = new FileOutputStream(outputFile);
             CipherOutputStream cipherOut = new CipherOutputStream(out, cipher)) {

            out.write(iv); // Write IV to the beginning of the output file

            byte[] buffer = new byte[8192]; // 8KB buffer
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                cipherOut.write(buffer, 0, bytesRead);
            }
        }
    }

    // Reading encrypted data from a file, decrypting it, and writing the decrypted data to another file.
    public static void decryptFile(String inputFile, String outputFile, String base64Key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

        try (FileInputStream in = new FileInputStream(inputFile);
             FileOutputStream out = new FileOutputStream(outputFile)) {

            byte[] iv = new byte[16]; // AES block size is 16 bytes
            in.read(iv); // Read IV from the beginning of the input file
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            try (CipherInputStream cipherIn = new CipherInputStream(in, cipher)) {
                byte[] buffer = new byte[8192]; // 8KB buffer
                int bytesRead;

                while ((bytesRead = cipherIn.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
        }
    }

    /*-------------------------- CREATE SAMPLE LARGE FILE FOR TESTING ----------------------------------------------*/
    public static void createLargeFile(String fileName, long size) {
        try (FileOutputStream out = new FileOutputStream(fileName)) {
            byte[] buffer = new byte[8192]; // 8KB buffer
            Random random = new Random();
            long bytesWritten = 0;

            while (bytesWritten < size) {
                random.nextBytes(buffer);
                out.write(buffer);
                bytesWritten += buffer.length;
            }

            System.out.println("File created successfully.");

            // NOW ENCRYPT THE CREATED FILE !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

            String encryptedFile = "encrypted_output.txt";
            String decryptedFile = "decrypted_output.txt";
            // Generate a random 16-byte key for AES
            byte[] keyBytes = new byte[16];
            new SecureRandom().nextBytes(keyBytes);
            String key = Base64.getEncoder().encodeToString(keyBytes);

            try {
                encryptFile(fileName, encryptedFile, key);
                System.out.println("File encrypted successfully.");

                decryptFile(encryptedFile, decryptedFile, key);
                System.out.println("File decrypted successfully.");
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}