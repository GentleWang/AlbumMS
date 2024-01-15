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

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SmbService {

    String equiptIp = "192.168.124.20";
    String authUserName = "****";
    String authPwd = "*****";
    String shareDir = "photos";

    public Map<String, List<String>> getSMBFileHash(String filePathDir, PrintWriter writer) throws IOException {
        SmbConfig config = SmbConfig.builder()
                .withTimeout(12000, TimeUnit.MINUTES) // Timeout sets Read, Write, and Transact timeouts (default is 60 seconds)
                .withSoTimeout(18000, TimeUnit.MINUTES) // Socket Timeout (default is 0 seconds, blocks forever)
                .build();
        SMBClient client = new SMBClient(config);
        ConcurrentHashMap<String, List<String>> md5s = new ConcurrentHashMap<>();
        try (Connection connection = client.connect(equiptIp)) {
            AuthenticationContext ac = new AuthenticationContext(authUserName, authPwd.toCharArray(), equiptIp);
            Session session = connection.authenticate(ac);
            ExecutorService executor = Executors.newFixedThreadPool(10); // 创建一个固定大小的线程池
            // Connect to Share
            try (DiskShare dirShare = (DiskShare) session.connectShare("photos")) {
                scanFileAndGetHash(dirShare, filePathDir, md5s, writer, executor);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                executor.shutdown(); // 关闭线程池
                while (!executor.isTerminated()) ; // 等待所有任务完成
                session.close();
                connection.close();
                client.close();
            }
        }
        return md5s;
    }

    public static void scanFileAndGetHash(DiskShare dirShare, String filePathDir, Map<String, List<String>> md5s, PrintWriter writer, ExecutorService executor) {
        //如果是个文件
        if (dirShare.fileExists(filePathDir)) {
            executor.submit(() -> {
                try {
                    // 这里就可以拿到文件流了，可以读取
                    com.hierynomus.smbj.share.File smbFileRead = dirShare.openFile(filePathDir, EnumSet.of(AccessMask.GENERIC_READ), null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null);
                    String fileMD5 = FileDigest.getFileHash(smbFileRead.getInputStream());
                    List<String> existingFile = md5s.putIfAbsent(fileMD5, Lists.newArrayList(filePathDir));
                    if (CollectionUtils.isNotEmpty(existingFile)) {
                        writer.print("Duplicate file found: " + filePathDir + " ");
                        writer.println("It is a duplicate of: " + existingFile);
                        existingFile.add(filePathDir);
                        md5s.put(fileMD5, existingFile);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else if (dirShare.folderExists(filePathDir)) {
            Directory directory = dirShare.openDirectory(filePathDir, EnumSet.of(AccessMask.FILE_LIST_DIRECTORY), null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null);
            List<FileIdBothDirectoryInformation> list = directory.list();
            for (FileIdBothDirectoryInformation f : list) {
                String fileName = f.getFileName(); // 文件名字
                String fileUrl = filePathDir + "/" + fileName;

                if (!".".equals(fileName) && !"..".equals(fileName)) {
                    scanFileAndGetHash(dirShare, fileUrl, md5s, writer, executor);
                }
            }
        }
    }

}
