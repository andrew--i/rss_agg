package org.ai.sse.controller;

import org.ai.model.Feed;
import org.ai.storage.FeedStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Date;
import java.util.List;

@RestController
public class FluxFeedController {

    @Autowired
    private FeedStorageService feedStorageService;

    @GetMapping(value = "/feed-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Feed> streamEvents() {

        final Date[] lastUpdate = new Date[]{null};
        return Flux.interval(Duration.ofSeconds(1))
                .flatMap(s -> {
                    final List<Feed> feeds;
                    if (s == 0) {
                        feeds = feedStorageService.getFeeds();
                    } else {
                        feeds = feedStorageService.getFeedsFrom(lastUpdate[0]);

                    }
                    if (!CollectionUtils.isEmpty(feeds))
                        lastUpdate[0] = feeds.get(feeds.size() - 1).getDate();

                    return Flux.just(feeds.toArray(new Feed[0]));
                });
    }
}
