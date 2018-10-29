package org.ai;

import org.ai.storage.FeedDownloader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class ScheduledTasks {

    private final FeedDownloader feedDownloader;

    private final Config config;

    @Autowired
    public ScheduledTasks(FeedDownloader feedDownloader, Config config) {
        this.feedDownloader = feedDownloader;
        this.config = config;
    }

    @Scheduled(fixedRate = 5000)
    public void reportCurrentTime() {

        Arrays.stream(config.getFeeds())
                .forEach(feedDownloader::startDownloading);

    }
}
