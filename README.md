## Cloud Sync

This is a tool used to backup local files to cloud object storage, currently supports Tencent cloud COS.

### Environment
- JDK: 17
- Maven: 3.8.1
- STS: 4.18.1.RELEASE
- OS: Ubuntu 21.04

### Build
```
git@github.com:wangxiaohui2015/cloud_sync.git
mvn clean package
```
After building, go to 'target/release' folder to check configuration files and startup scripts.

Update conf/config.properties according to your settings.

Then execute start.sh to start backup.
