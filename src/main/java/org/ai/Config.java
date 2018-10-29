package org.ai;


import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@ConfigurationProperties(prefix = "rss.agg")
public class Config implements InitializingBean {
    private String[] feeds;
    private Integer connectTimeout;
    private Integer socketTimeout;
    private Integer maxConnections = 1;
    private String storageDir;

    public String[] getFeeds() {
        return feeds;
    }

    public void setFeeds(String[] feeds) {
        this.feeds = feeds;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Integer getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(Integer socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public Integer getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
    }

    public String getStorageDir() {
        return storageDir;
    }

    public void setStorageDir(String storageDir) {
        this.storageDir = storageDir;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        validateConfig();
    }

    private void validateConfig() {
        if(StringUtils.isEmpty(storageDir))
            throw new IllegalArgumentException("Storage Dir is Empty");
    }
}
