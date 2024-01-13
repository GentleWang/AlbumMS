package com.ws.albummange;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 判断文件相似度
 */
public class FileDigest {
    /**
     * 获取单个文件的MD5值！
     *
     * @param file
     * @return
     */
    public static String getFileHash(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }

    /**
     * 获取单个文件的MD5值！
     *
     * @param inputStream
     * @return
     */
    public static String getFileHash(InputStream inputStream) {
        MessageDigest digest = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            while ((len = inputStream.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }

    private static String getFiles(File file) throws Exception {
        FileReader reader = new FileReader(file);//定义一个fileReader对象，用来初始化BufferedReader
        BufferedReader bReader = new BufferedReader(reader);//new一个BufferedReader对象，将文件内容读取到缓存
        StringBuilder sb = new StringBuilder();//定义一个字符串缓存，将字符串存放缓存中
        String s = "";
        while ((s = bReader.readLine()) != null) {//逐行读取文件内容，不读取换行符和末尾的空格
            sb.append(s + "\n");//将读取的字符串添加换行符后累加存放在缓存中
            System.out.println(s);
        }
        bReader.close();
        String str = sb.toString();
        return str;
    }


    private static ArrayList<String> getPathNames(String path) throws Exception {
        ArrayList<String> fileStr = new ArrayList<>();
        File pathFile = new File(path);
        if (pathFile.isDirectory()) {
            File[] files = pathFile.listFiles();
            for (File file : files) {
                fileStr.add(file.getAbsolutePath());
            }
        }
        return fileStr;
    }

    public static void scanFileAndGetMD5(File pathFile, Map<String, String> md5s) {
        if (pathFile.isDirectory()) {
            File[] files = pathFile.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    scanFileAndGetMD5(file, md5s);
                } else {
                    File file1 = new File(file + "");
                    md5s.put(file.getAbsolutePath(), getFileHash(file1));
                }
            }
        }
    }



    public static void main(String[] args) throws Exception {
        String path = "/Volumes/My Passport/相册";
        File pathFile = new File(path);
        Map<String, String> md5s = new HashMap<>();
        scanFileAndGetMD5(pathFile, md5s);

        ArrayList<Map.Entry> md5EntryList = new ArrayList<>(md5s.entrySet());
        int md5Size = md5EntryList.size();
        System.out.println("照片数：" + md5Size);
        int doubleCount = 0;


        for (int i = 0; i < md5Size - 1; i++) {
            Map.Entry md5 = md5EntryList.get(i);
            for (int j = i + 1; j < md5Size; j++) {
                if (md5.getValue().equals(md5EntryList.get(j).getValue())) {
                    String srcFile = md5.getKey() + "";
                    String checkResult = String.format("%s与%s一样", srcFile, md5EntryList.get(j).getKey());
                    System.out.println(checkResult);
                    doubleCount++;
                }
            }
        }
        System.out.println("重复数：" + doubleCount);

    }


}