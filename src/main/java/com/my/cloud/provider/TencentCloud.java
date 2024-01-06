
package com.my.cloud.provider;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.StorageClass;
import com.qcloud.cos.region.Region;

/**
 * Tencent Cloud.
 * 
 * @author Sunny
 */
public class TencentCloud implements ICloudProvider {

    private static Logger logger = Logger.getLogger(TencentCloud.class);

    private String secretId;
    private String secretKey;
    private String region;
    private String bucket;
    private String storage;

    private COSClient cosclient = null;

    public TencentCloud(String secretId, String secretKey, String region, String bucket,
                    String storage) {
        this.secretId = secretId;
        this.secretKey = secretKey;
        this.region = region;
        this.bucket = bucket;
        this.storage = storage;
        init();
    }

    private void init() {
        COSCredentials cred = new BasicCOSCredentials(this.secretId, this.secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(this.region));
        this.cosclient = new COSClient(cred, clientConfig);
    }

    @Override
    public void verifyBucket() throws Exception {
        cosclient.getBucketAcl(this.bucket);
    }

    @Override
    public Map<String, String> uploadFile(String key, File file) throws Exception {
        Map<String, String> metaData = new HashMap<String, String>();
        if (null == file) {
            String errorMsg = "file is null, skip upload.";
            logger.error(errorMsg);
            throw new Exception(errorMsg);
        }
        if (!file.isFile() || !file.exists()) {
            String errorMsg = "File doesn't exist or it's not file, skip upload, "
                            + file.getAbsolutePath();
            logger.error(errorMsg);
            throw new Exception(errorMsg);
        }
        PutObjectRequest putObjectRequest = new PutObjectRequest(this.bucket, key, file);
        putObjectRequest.setStorageClass(getStorageClass());
        PutObjectResult putObjectResult = cosclient.putObject(putObjectRequest);

        metaData.put("etag", putObjectResult.getETag());
        metaData.put("crc64", putObjectResult.getCrc64Ecma());
        return metaData;
    }

    @Override
    public void closeConnection() throws Exception {
        cosclient.shutdown();
    }

    private StorageClass getStorageClass() {
        StorageClass storageClass = null;
        switch (this.storage) {
            case "standard":
                storageClass = StorageClass.Standard;
                break;
            case "standard_ia":
                storageClass = StorageClass.Standard_IA;
                break;
            case "archive":
                storageClass = StorageClass.Archive;
                break;
            case "deep_archive":
                storageClass = StorageClass.Deep_Archive;
                break;
            default:
                logger.warn("Unknown storage type: " + this.storage + ", will use default: standard");
                storageClass = StorageClass.Standard;
        }
        return storageClass;
    }
}
