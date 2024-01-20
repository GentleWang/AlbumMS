package com.ws.albummange;

import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LocalDupplicatePhotoCheck {


    public static void main(String[] args) throws Exception {
        LocalFileHashService localFileHashService = new LocalFileHashService();

        Map<String, List<String>> map = new HashMap<>();
        try (PrintWriter writer = new PrintWriter(new File("duplicates.txt"))) {
            map = localFileHashService.getSMBFileHash("I:\\相册\\手机备份所有照片\\查重");
            Set<Map.Entry<String, List<String>>> entrySet = map.entrySet();
            for (Map.Entry<String, List<String>> stringListEntry : entrySet) {
                if (CollectionUtils.isNotEmpty(stringListEntry.getValue()) && stringListEntry.getValue().size() > 1) {
                writer.println(stringListEntry.getKey() + "#" + stringListEntry.getValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


//        System.out.println(map);

//        ArrayList<Map.Entry> md5EntryList = new ArrayList<>(map.entrySet());
//        int md5Size = md5EntryList.size();
//        System.out.println("照片数：" + md5Size);
//        int doubleCount = 0;
//
//
//        for (int i = 0; i < md5Size - 1; i++) {
//            Map.Entry md5 = md5EntryList.get(i);
//            for (int j = i + 1; j < md5Size; j++) {
//                if (md5.getValue().equals(md5EntryList.get(j).getValue())) {
//                    String srcFile = md5.getKey() + "";
//                    String checkResult = String.format("%s与%s一样", srcFile, md5EntryList.get(j).getKey());
//                    System.out.println(checkResult);
//                    doubleCount++;
//                }
//            }
//        }
//        System.out.println("重复数：" + doubleCount);

    }
}
