package com.ws.albummange;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;


import java.io.*;
import java.util.List;

public class SardineService {


    public static void main(String[] args) throws IOException {
//        Sardine sardine = SardineFactory.begin("admin", "admin");
        Sardine sardine = SardineFactory.begin();

        if (sardine.exists("smb://192.168.124.20")) {
            System.out.println("exists");
        }

//        sardine.createDirectory("http://192.168.1.71:4502/crx/repository/crx.default/content/dam/testfolder/");
//
//        InputStream fis = new FileInputStream(new File("img12.jpg"));
//        sardine.put("http://192.168.1.71:4502/crx/repository/crx.default/content/dam/testfolder/img12.jpg", fis);
//
//        List<DavResource> resources = sardine.getResources("http://192.168.1.71:4502/crx/repository/crx.default/content/dam/testfolder/");
//        for (DavResource res : resources)
//        {
//            System.out.println(res); // calls the .toString() method.
//        }
    }
}
