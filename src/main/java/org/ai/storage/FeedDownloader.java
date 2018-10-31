package org.ai.storage;

import org.ai.Config;
import org.ai.model.Feed;
import org.ai.ssl.SSLUtils;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.methods.AsyncByteConsumer;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class FeedDownloader implements DisposableBean {

    private final static Logger LOGGER = LoggerFactory.getLogger(FeedDownloader.class);


    private final FeedStorageService feedStorageService;
    private final CloseableHttpAsyncClient httpClient;

    private final ConcurrentMap<String, ReentrantLock> lockHashMap = new ConcurrentHashMap<>();

    @Autowired
    public FeedDownloader(Config config, FeedStorageService feedStorageService) {

        final Integer connectTimeout = config.getConnectTimeout();
        final Integer socketTimeout = config.getSocketTimeout();
        final IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setConnectTimeout(connectTimeout == null ? 0 : connectTimeout)
                .setSoTimeout(socketTimeout == null ? 0 : socketTimeout)
                .build();

        final Integer maxConnections = config.getMaxConnections();
        this.httpClient = HttpAsyncClients
                .custom()
                .setDefaultIOReactorConfig(ioReactorConfig)
                .setMaxConnPerRoute(maxConnections == null ? 0 : maxConnections)
                .setMaxConnTotal(maxConnections == null ? 0 : maxConnections)
                .setSSLContext(SSLUtils.getAllTrustedSSLContext())
                .build();

        this.feedStorageService = feedStorageService;
    }

    private ReentrantLock createReentrantLocker(String key) {
        if (!lockHashMap.containsKey(key)) {
            lockHashMap.put(key, new ReentrantLock());
        }

        return lockHashMap.get(key);
    }


    public void startDownloading(String feedUrl) {

        LOGGER.info("Downloading feed " + feedUrl);

        final ReentrantLock locker = createReentrantLocker(feedUrl);

        if (locker.tryLock()) {

            try {
                if (!httpClient.isRunning())
                    httpClient.start();
                final String resourcePath = feedStorageService.getResourcePath(feedUrl);

                final Feed item = new Feed();
                item.setSrc(feedUrl);
                item.setStoragePath(resourcePath);

                httpClient.execute(HttpAsyncMethods.createGet(feedUrl),
                        new SourceFeedConsumer(item),
                        new FutureCallback<Feed>() {
                            @Override
                            public void completed(Feed o) {
                                feedStorageService.complete(item);
                            }

                            @Override
                            public void failed(Exception e) {
                                LOGGER.error("Could not download resource " + feedUrl, e);
                                feedStorageService.fail(feedUrl, e.getMessage());
                            }

                            @Override
                            public void cancelled() {
                                feedStorageService.cancel(feedUrl);
                            }
                        }
                );
            } finally {
                locker.unlock();
            }
        } else {
            LOGGER.info("Downloading in progress " + feedUrl);

        }


    }

    @Override
    public void destroy() throws Exception {

        try {
            httpClient.close();
        } catch (IOException e) {
            LOGGER.info("Could not close http client", e);
        }
    }


    private class SourceFeedConsumer extends AsyncByteConsumer<Feed> {

        private WritableByteChannel writeChannel;

        private Feed item;

        SourceFeedConsumer(Feed item) {
            this.item = item;
        }

        @Override
        protected void onResponseReceived(HttpResponse response) throws IOException {
            writeChannel = Channels.newChannel(new BufferedOutputStream(new FileOutputStream(item.getStoragePath())));
        }

        @Override
        protected Feed buildResult(HttpContext context) {
            return item;
        }

        @Override
        protected void onByteReceived(ByteBuffer buf, IOControl ioctrl) throws IOException {
            writeChannel.write(buf);
        }

        @Override
        protected void releaseResources() {
            try {
                if (writeChannel != null)
                    writeChannel.close();
            } catch (IOException e) {
                LOGGER.info("Could not close channel", e);
            }
        }

    }
}
