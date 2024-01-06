
package com.my.cloud.provider;

import java.io.File;
import java.util.Map;

/**
 * ICloud provider.
 * 
 * @author Sunny
 */
public interface ICloudProvider {
    void verifyBucket() throws Exception;

    public Map<String, String> uploadFile(String key, File file) throws Exception;

    void closeConnection() throws Exception;
}
