package com.my.cloud.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.my.cloud.provider.ICloudProvider;
import com.my.cloud.provider.TencentCloud;
import com.my.proxy.util.ConfigUtil;

/**
 * Backup service.
 * 
 * @author Sunny
 */
public class BackupService {

    private static Logger logger = Logger.getLogger(BackupService.class);

    private static final String CLOUD_PROVIDER_TENCENT = "tencent";

    public void startBackup() {
        try {
            logger.info("Begin to backup...");
            List<String> backupFileList = generateBackupFileList();
            List<String> lastSucceedFileList = loadSucceedFileList();
            List<String> diffFileList = generateDiffFileList(backupFileList, lastSucceedFileList);
            List<String> succeedList = uploadFiles(diffFileList);
            updateRecordFile(succeedList);
            logger.info("Finished all backups.");
        } catch (Exception e) {
            logger.error("Failed to backup.", e);
        }
    }

    private void updateRecordFile(List<String> fileList) throws IOException {
        if (fileList.isEmpty()) {
            logger.info("File list is empty, skip to update record file.");
            return;
        }
        File file = new File(ConfigUtil.getInstance().getSucceedRecordFile());
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                logger.error("Failed to create record file: " + file.getAbsolutePath(), e);
                throw e;
            }
        }
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write("#### - > New backup record begin, date time: " + new Date().getTime()
                            + "\n");
            for (String succeedFile : fileList) {
                writer.write(succeedFile + "\n");
            }
            writer.write("#### - > New backup record end, date time: " + new Date().getTime()
                            + "\n");
            writer.flush();
        } catch (IOException e) {
            logger.error("Failed to write succeed upload file list.", e);
            throw e;
        }
    }

    private List<String> uploadFiles(List<String> fileList) throws Exception {
        List<String> results = new ArrayList<String>();
        if (fileList.isEmpty()) {
            logger.info("File list is empty, skip to upload.");
            return results;
        }
        ICloudProvider provider = getProvider();
        logger.info("Cloud provider: " + provider.toString());
        try {
            for (String file : fileList) {
                String localFilePath = ConfigUtil.getInstance().getSrcPath() + file;
                try {
                    Map<String, String> metaData =
                                    provider.uploadFile(file, new File(localFilePath));
                    results.add(file);
                    String metaInfo = genMetaInfo(metaData);
                    logger.info("Succeed to upload file: " + localFilePath + ", key: " + file + ", "
                                    + metaInfo);
                } catch (Exception e) {
                    logger.error("Failed to upload file: " + localFilePath + ", key: " + file, e);
                }
            }
        } finally {
            if (provider != null) {
                provider.closeConnection();
            }
        }
        return results;
    }

    private String genMetaInfo(Map<String, String> metaData) {
        Set<String> keys = metaData.keySet();
        String metaInfo = "";
        for (String key : keys) {
            metaInfo += (key + ": " + metaData.get(key) + ", ");
        }
        if (!metaInfo.isEmpty()) {
            // Remove the last ", ".
            metaInfo = metaInfo.substring(0, metaInfo.length() - 2);
        }
        return metaInfo;
    }

    private List<String> generateDiffFileList(List<String> fileList,
                    List<String> lastSucceedFileList) {
        List<String> results = new ArrayList<String>();
        fileList.stream().filter(i -> !lastSucceedFileList.contains(i))
                        .forEach(j -> results.add(j));
        return results;
    }

    private List<String> loadSucceedFileList() throws Exception {
        List<String> results = new ArrayList<String>();
        File file = new File(ConfigUtil.getInstance().getSucceedRecordFile());
        if (!file.exists()) {
            return results;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("####")) { // Skip comment line
                    continue;
                }
                results.add(line);
            }
        } catch (FileNotFoundException e) {
            logger.error("Failed to load succeed record file list.");
            throw e;
        }
        return results;
    }

    private List<String> generateBackupFileList() throws Exception {
        String basePath = ConfigUtil.getInstance().getSrcPath();
        File file = new File(basePath);
        if (!file.exists() || !file.isDirectory()) {
            String errorMsg = "Src path doesn't exit or it's not a folder, src path: " + basePath;
            logger.error(errorMsg);
            throw new Exception(errorMsg);
        }
        return doGenerateBackupFiles(basePath, basePath);
    }

    private List<String> doGenerateBackupFiles(String basePath, String absPath) {
        List<String> results = new ArrayList<String>();
        File sourceFile = new File(absPath);
        File[] files = sourceFile.listFiles();
        if (null == files) {
            return results;
        }
        for (File file : files) {
            if (file.isFile()) {
                String fileAbsPath = file.getAbsolutePath();
                String fileRelPath = fileAbsPath.substring(basePath.length());
                results.add(fileRelPath);
            } else {
                results.addAll(doGenerateBackupFiles(basePath, file.getAbsolutePath()));
            }
        }
        return results;
    }

    private ICloudProvider getProvider() throws Exception {
        ICloudProvider provider = null;
        ConfigUtil config = ConfigUtil.getInstance();
        if (config.getCloud().equals(CLOUD_PROVIDER_TENCENT)) {
            provider = new TencentCloud(config.getSecretId(), config.getSecretKey(),
                            config.getRegion(), config.getBucket(), config.getStorage());
        }
        if (null == provider) {
            String errMsg = "Cannot get cloud provider.";
            logger.error(errMsg);
            throw new Exception(errMsg);
        }
        try {
            provider.verifyBucket();
        } catch (Exception e) {
            logger.error("Failed to verify provider bucket, please check configurations of bucket name, region, secret id and secret key.",
                            e);
            throw e;
        }
        return provider;
    }
}
