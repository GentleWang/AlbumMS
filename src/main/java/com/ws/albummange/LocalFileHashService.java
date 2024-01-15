package com.ws.albummange;

import com.google.common.collect.Lists;
import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.Directory;
import com.hierynomus.smbj.share.DiskShare;
import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LocalFileHashService {


    public Map<String, List<String>> getSMBFileHash(String filePathDir) throws IOException {
        ConcurrentHashMap<String, List<String>> md5s = new ConcurrentHashMap<>();

        ExecutorService executor = Executors.newFixedThreadPool(10); // 创建一个固定大小的线程池
        try  {
            scanFileAndGetHash(filePathDir, md5s, executor);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            executor.shutdown(); // 关闭线程池
            while (!executor.isTerminated()) ; // 等待所有任务完成

        }

        return md5s;
    }

    public static void scanFileAndGetHash(String filePathDir, Map<String, List<String>> md5s, ExecutorService executor) {
        File file = new File(filePathDir);
        //如果是个文件
        if (file.isFile()) {
            executor.submit(() -> {
                try {
                    // 这里就可以拿到文件流了，可以读取
                    String fileMD5 = FileDigest.getFileHash(file);
                    List<String> existingFile = md5s.putIfAbsent(fileMD5, Lists.newArrayList(filePathDir));
                    if (CollectionUtils.isNotEmpty(existingFile)) {
                        System.out.print("Duplicate file found: " + filePathDir + " ");
                        System.out.println("It is a duplicate of: " + existingFile);
                        existingFile.add(filePathDir);
                        md5s.put(fileMD5, existingFile);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else if (file.isDirectory()) {
            File[] fileList = file.listFiles();
            for (File f : fileList) {
                String fileName = f.getName(); // 文件名字
                String fileUrl = filePathDir + "/" + fileName;

                if (!".".equals(fileName) && !"..".equals(fileName)) {
                    scanFileAndGetHash(fileUrl, md5s, executor);
                }
            }
        }
    }

}
