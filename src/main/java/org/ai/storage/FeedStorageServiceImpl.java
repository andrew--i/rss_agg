package org.ai.storage;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import org.ai.Config;
import org.ai.model.Feed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class FeedStorageServiceImpl implements FeedStorageService, InitializingBean {

    private final static Logger LOGGER = LoggerFactory.getLogger(FeedStorageServiceImpl.class);

    private final Config config;

    private final Object operationLocker = new Object();

    private Set<Feed> feedSet;

    @Autowired
    public FeedStorageServiceImpl(Config config) {
        final String storageDir = config.getStorageDir();
        final File storage = new File(storageDir);
        if (!storage.exists())
            storage.mkdir();

        this.config = config;
    }

    @Override
    public String getResourcePath(String feedUrl) {
        final String dirName;

        try {
            dirName = URLEncoder.encode(feedUrl, Charset.defaultCharset().toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        final String storageDir = config.getStorageDir();

        return storageDir + File.separator + dirName + File.separator + System.currentTimeMillis() + ".xml";
    }

    @Override
    public void complete(Feed item) {
        synchronized (operationLocker) {
            final String storagePath = item.getStoragePath();
            final File file = new File(storagePath);
            final List<Feed> feeds = readFeeds(file, item.getSrc());
            feedSet.addAll(feeds);
        }
    }

    @Override
    public void fail(String feedUrl, String message) {
        LOGGER.warn(String.format("Could not download feed by %s:\n%s", feedUrl, message));
    }

    @Override
    public void cancel(String feedUrl) {
        LOGGER.warn(String.format("Feed downloading canceled %s", feedUrl));
    }

    private List<Feed> getFeeds() {
        final ArrayList<Feed> feeds;
        synchronized (operationLocker) {
            feeds = new ArrayList<>(feedSet);
        }
        feeds.sort(Comparator.comparing(Feed::getDate));
        return feeds;
    }

    @Override
    public List<Feed> getFeedsAfter(LocalDateTime date) {
        final List<Feed> feeds = getFeedsFrom(date);
        if (date != null)
            feeds.removeIf(f -> f.getDate().compareTo(date) == 0);
        return feeds;
    }

    @Override
    public List<Feed> getFeedsFrom(LocalDateTime date) {
        if (date == null)
            return getFeeds();
        final ArrayList<Feed> feeds;
        synchronized (operationLocker) {
            feeds = new ArrayList<>(feedSet);
        }
        feeds.removeIf(f -> f.getDate().compareTo(date) < 0);
        return feeds;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        feedSet = readStorage();
    }

    private Set<Feed> readStorage() throws UnsupportedEncodingException {
        final Set<Feed> result = new HashSet<>();
        synchronized (operationLocker) {
            final String[] feeds = config.getFeeds();
            for (int i = feeds.length - 1; i > -1; i--) {
                final String feed = feeds[i];
                final String dirName = URLEncoder.encode(feed, Charset.defaultCharset().toString());
                final File dir = new File(config.getStorageDir() + File.separator + dirName);
                if (dir.exists()) {
                    final File[] files = dir.listFiles();
                    if (files != null) {
                        final List<Feed> list = Arrays.stream(files)
                                .flatMap(f -> readFeeds(f, feed).stream())
                                .collect(Collectors.toList());
                        result.addAll(list);
                    }
                } else {
                    dir.mkdir();
                }
            }

        }

        return result;
    }

    private List<Feed> readFeeds(File file, String src) {
        final SyndFeed feed;
        try {
            feed = new SyndFeedInput().build(file);
        } catch (IOException | FeedException e) {
            LOGGER.warn("Could not read feed", e);
            return Collections.emptyList();
        }

        return feed.getEntries().stream().map(e -> {
            final Feed f = new Feed();
            f.setDate(LocalDateTime.ofInstant(e.getPublishedDate().toInstant(), ZoneId.systemDefault()));
            f.setLink(e.getLink());
            f.setTitle(e.getTitle());
            f.setSrc(src);
            f.setStoragePath(file.getAbsolutePath());
            return f;
        }).collect(Collectors.toList());

    }
}
