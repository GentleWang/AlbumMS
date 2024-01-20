package com.ws.albummange;

import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

public class FileIOService {
    /**
     * 把检查结果写入查重文件
     * @param checkFilePath
     * @param checkResultMap
     */
    public void writeResult(String checkFilePath,Map<String,Set<String>> checkResultMap){

        try (PrintWriter writer = new PrintWriter(checkFilePath)) {
            Set<Map.Entry<String, Set<String>>> entrySet = checkResultMap.entrySet();
            for (Map.Entry<String, Set<String>> stringListEntry : entrySet) {
                if (CollectionUtils.isNotEmpty(stringListEntry.getValue()) && stringListEntry.getValue().size() > 1) {
                    writer.println(stringListEntry.getKey() + "#" + stringListEntry.getValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
