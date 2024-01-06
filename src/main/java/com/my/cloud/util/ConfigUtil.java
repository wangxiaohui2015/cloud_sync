package com.my.cloud.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.my.cloud.Main;

/**
 * Configuration utility tool.
 * 
 * @author Sunny
 */
public class ConfigUtil {

    private static Logger logger = Logger.getLogger(ConfigUtil.class);

    private static final ConfigUtil instance = new ConfigUtil();
    private static Properties properties = new Properties();

    private static final String KEY_CLOUD = "cloud";
    private static final String KEY_SRC_PATH = "src.path";
    private static final String KEY_THREADS = "threads";
    private static final String KEY_SECRETID = "secretId";
    private static final String KEY_SECRETKEY = "secretKey";
    private static final String KEY_REGION = "region";
    private static final String KEY_BUCKET = "bucket";
    private static final String KEY_STORAGE = "storage";

    private String cloud = "tencent";
    private String srcPath = "";
    private int threads = 3;
    private String secretId = "";
    private String secretKey = "";
    private String region = "";
    private String bucket = "";
    private String storage = "standard";

    public static final ConfigUtil getInstance() {
        return instance;
    }

    public String getSrcPath() {
        return srcPath;
    }

    public int getThreads() {
        return threads;
    }

    public String getSecretId() {
        return secretId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getCloud() {
        return cloud;
    }

    public String getRegion() {
        return region;
    }

    public String getBucket() {
        return bucket;
    }

    public String getStorage() {
        return storage;
    }

    public String getSucceedRecordFile() {
        String recordFile = Main.getRootDir() + File.separator + "conf" + File.separator
                        + "succeed_record.txt";
        return recordFile;
    }

    public void init() throws Exception {
        String configFilePath = Main.getRootDir() + File.separator + "conf" + File.separator
                        + "config.properties";
        File configFile = new File(configFilePath);
        if (!configFile.exists()) {
            String errorMsg = "configuration file doesn't exist, " + configFilePath;
            logger.error(errorMsg);
            throw new Exception(errorMsg);
        }

        try {
            InputStream in = new FileInputStream(configFilePath);
            properties.load(in);
            initCloud();
            initSrcPath();
            initThreads();
            initSecretId();
            initSecretKey();
            initRegion();
            initBucket();
            initStorage();
        } catch (Exception e) {
            logger.error("Failed to init ConfigUtil, service exits.", e);
            throw e;
        }
    }

    private ConfigUtil() {}

    private void initCloud() {
        String strCloud = properties.getProperty(KEY_CLOUD);
        if (null == strCloud || "".equals(strCloud)) {
            return;
        }
        cloud = strCloud;
    }

    private void initSrcPath() {
        String path = properties.getProperty(KEY_SRC_PATH);
        if (null == path || "".equals(path)) {
            return;
        }
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        srcPath = path;
    }

    private void initThreads() {
        String strThreads = properties.getProperty(KEY_THREADS);
        if (null == strThreads || "".equals(strThreads)) {
            return;
        }
        try {
            threads = Integer.parseInt(strThreads);
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse strThreads, will use default value.", e);
        }
    }

    private void initSecretId() {
        String sId = properties.getProperty(KEY_SECRETID);
        if (null == sId || "".equals(sId)) {
            return;
        }
        secretId = sId;
    }

    private void initSecretKey() {
        String sKey = properties.getProperty(KEY_SECRETKEY);
        if (null == sKey || "".equals(sKey)) {
            return;
        }
        secretKey = sKey;
    }

    private void initRegion() {
        String strRegion = properties.getProperty(KEY_REGION);
        if (null == strRegion || "".equals(strRegion)) {
            return;
        }
        region = strRegion;
    }

    private void initBucket() {
        String strBucket = properties.getProperty(KEY_BUCKET);
        if (null == strBucket || "".equals(strBucket)) {
            return;
        }
        bucket = strBucket;
    }

    private void initStorage() {
        String strStorage = properties.getProperty(KEY_STORAGE);
        if (null == strStorage || "".equals(strStorage)) {
            return;
        }
        storage = strStorage;
    }
}
