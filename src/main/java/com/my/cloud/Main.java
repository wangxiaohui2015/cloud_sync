package com.my.cloud;

import java.io.File;

import org.apache.log4j.Logger;

import com.my.cloud.service.BackupService;
import com.my.cloud.util.ConfigUtil;

/**
 * This is the main entry, please set root directory when running this program, below is an example,
 * -DrootDir=/home/user/work/root_dir
 * 
 * </br>
 * This is to make sure the path of configuration is is '<rootDir>/conf/config.properties'
 * 
 * @author Sunny
 */
public class Main {

    private static Logger logger;

    private static String rootDir = "";

    public static void main(String[] args) {
        init(args);
        new BackupService().startBackup();
    }

    public static String getRootDir() {
        return rootDir;
    }

    private static void init(String[] args) {
        rootDir = System.getProperty("rootDir");
        if (null == rootDir || "".equals(rootDir) || !new File(rootDir).isDirectory()) {
            String errorInfo = "ERROR: rootDir is not set or not exist, system exits.";
            System.out.println(errorInfo);
            System.exit(-1);
        }
        try {
            logger = Logger.getLogger(Main.class);
            ConfigUtil.getInstance().init();
        } catch (Exception e) {
            logger.error("Failed to initialize system.", e);
            System.exit(-1);
        }
    }
}
