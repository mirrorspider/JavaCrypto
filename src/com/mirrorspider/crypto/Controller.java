package com.mirrorspider.crypto;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.NoSuchFileException;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

class Controller{
    
    private static String usage = "Usage:\nController [keygen|encrypt|decrypt [user]]";
    
    public static void main(String[] args) {
        
        if(args.length < 1 || args.length > 2){
            System.err.println(usage);
            System.exit(9);
        }
        
        File inputFile = new File("document.txt");
        File encryptedFile = new File("document.encrypted");
         
        try {
            Key ky;
            Path p = FileSystems.getDefault().getPath("./key.txt");

            if(args.length > 0 && args[0].equals("keygen")){
                KeyGenerator k = KeyGenerator.getInstance("AES");
                k.init(256);
                ky = k.generateKey();
                Files.write(p, ky.getEncoded(), StandardOpenOption.CREATE);
                System.out.println("Key generated");
            }
            
            
            byte[] b = Files.readAllBytes(p);
            ky = new SecretKeySpec(b, "AES");
            if(args.length > 0 && args[0].equals("encrypt")){
                CryptoUtils.encrypt(ky, inputFile, encryptedFile);
                System.out.println("file encrypted");
            }
            
            if(args.length > 0 && args[0].equals("decrypt")){
                String toFind = "(^.*$)";
                if(args.length == 2){
                    toFind = "^" + args[1] + ":(.*)$";
                }
                    
                String plaintext = CryptoUtils.decrypt(ky, encryptedFile);
                String[] plainlines = plaintext.split("\n");
                // DOTALL required to match new lines etc.
                Pattern ptn = Pattern.compile(toFind, Pattern.DOTALL);
                Matcher mtc;
                for(int i = 0; i < plainlines.length; i++){
                    plaintext = plainlines[i];
                    mtc = ptn.matcher(plaintext);
                    if(mtc.matches()){
                        System.out.println(mtc.group(1));
                    }
                }
            }
        }
        catch(NoSuchFileException ex){
            System.out.println("Key file missing, restore from Google drive.");
        }
        catch (CryptoException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
