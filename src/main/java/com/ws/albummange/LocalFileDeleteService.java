package com.ws.albummange;

import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Runtime.getRuntime;

public class LocalFileDeleteService {

    public void deleteFile(String parentPath,String checkResultPath) throws Exception {
//        Set<String> duppFileList = extractDuppFile(checkResultPath);
        Map<String,Set<String>> checkResultMap =  extractDuppFile2Map(checkResultPath);
        try(PrintWriter writer = new PrintWriter(new File("deleteDuppFile.sh"))){
            for (String hashKey : checkResultMap.keySet()) {
                Set<String> duppFileSet = checkResultMap.get(hashKey);
                Iterator iterator = duppFileSet.iterator();
                while (iterator.hasNext()){
                    String filePath = (String) iterator.next();
                    if(isFileInPath(filePath,parentPath)){
                        System.out.print("删除指定文件夹下的文件："+filePath);
                        writer.println("rm -f "+filePath);
                        Path path = Paths.get(filePath);
                        boolean deleteFlag = Files.deleteIfExists(path);
                        System.out.println(",删除状态："+deleteFlag);
                        if(!deleteFlag){
                            continue;
                        }
                        iterator.remove();
                        if(CollectionUtils.isNotEmpty(duppFileSet) && duppFileSet.size() >1){
                            checkResultMap.put(hashKey,duppFileSet);
                        }else{
                            checkResultMap.remove(hashKey);
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }

        FileIOService fileIOService = new FileIOService();
        fileIOService.writeResult(checkResultPath,checkResultMap);

    }

    /**
     * 检查文件是否在需要删除的路径中
     * @param filePath
     * @param parentPath
     * @return
     */
    public boolean isFileInPath(String filePath,String parentPath){
        File file = new File(filePath);
        if(file.getParentFile().getPath().equals(new File(parentPath).getPath())){
            return true;
        }
        return false;
    }

    /**
     * 读入验重文件
     */
    public Set<String> extractDuppFile(String checkResultFile) {
        HashSet<String> duppFileList = Sets.newHashSet();
        //防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw;
        //不关闭文件会导致资源的泄露，读写文件都同理
        //Java7的try-with-resources可以优雅关闭文件，异常时自动关闭文件；详细解读https://stackoverflow.com/a/12665271
        try (FileReader reader = new FileReader(checkResultFile);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            String line;
            //网友推荐更加简洁的写法
            while ((line = br.readLine()) != null) {
                // 一次读入一行数据
                //ef35015aa5a3f249b377ee08200409ba25991e76a1404fed294cd0b153dfd897:[/Volumes/My Passport/相册/D3100/2012-8-26动物园/DSC_0239.JPG, /Volumes/My Passport/相册/D3100/D3100/2012-8-26动物园/DSC_0239.JPG]
                String [] lineArray = line.split("#");
                if(lineArray.length == 2){
                    String duppFileArray = lineArray[1].replaceAll("\\[","").replaceAll("\\]","");
                    String[] fileNameList = duppFileArray.split(",");
                    if(fileNameList.length >= 2){
                        for (String s : fileNameList) {
                            duppFileList.add(s.trim());
                        }
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return duppFileList;
    }

    /**
     * 把验重文件解析成map
     * @param checkResultFile
     * @return
     */
    public Map<String,Set<String>> extractDuppFile2Map(String checkResultFile) {
        Map<String,Set<String>> checkResultMap = new ConcurrentHashMap<>();

        //防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw;
        //不关闭文件会导致资源的泄露，读写文件都同理
        //Java7的try-with-resources可以优雅关闭文件，异常时自动关闭文件；详细解读https://stackoverflow.com/a/12665271
        try (FileReader reader = new FileReader(checkResultFile);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            String line;
            //网友推荐更加简洁的写法
            while ((line = br.readLine()) != null) {
                // 一次读入一行数据
                //ef35015aa5a3f249b377ee08200409ba25991e76a1404fed294cd0b153dfd897:[/Volumes/My Passport/相册/D3100/2012-8-26动物园/DSC_0239.JPG, /Volumes/My Passport/相册/D3100/D3100/2012-8-26动物园/DSC_0239.JPG]
                String [] lineArray = line.split("#");
                if(lineArray.length == 2){
                    HashSet<String> duppFileList = Sets.newHashSet();
                    String hashKey = lineArray[0].trim();
                    String duppFileArray = lineArray[1].replaceAll("\\[","").replaceAll("\\]","");
                    String[] fileNameList = duppFileArray.split(",");
                    if(fileNameList.length >= 2){
                        for (String s : fileNameList) {
                            duppFileList.add(s.trim());
                        }
                        Set<String> existingFile = checkResultMap.putIfAbsent(hashKey,duppFileList);
                        if(CollectionUtils.isNotEmpty(existingFile)){
                            existingFile.addAll(duppFileList);
                            checkResultMap.put(hashKey,existingFile);
                        }
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return checkResultMap;
    }

    /**
     * 设置文件权限
     * @param dirPath
     * @throws IOException
     */
    private void changeFolderPermission(String dirPath) throws IOException {
        Runtime runtime = getRuntime();
        String command = "sudo chmod 770 " + dirPath;
        try {
            Process process = runtime.exec(command);
            process.waitFor();
            int existValue = process.exitValue();
            if(existValue != 0){
                System.out.println( "Change file permission failed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println( "Command execute failed.");
        }
    }

    public static void main(String[] args) {
        LocalFileDeleteService localFileDeleteService = new LocalFileDeleteService();
        try{
            localFileDeleteService.deleteFile("I:\\相册\\手机备份所有照片\\查重\\周艳iPhone8 20191213/屏幕快照","duplicates.txt");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
