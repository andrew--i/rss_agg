package org.ai.sse.controller;

import org.ai.model.Feed;
import org.ai.storage.FeedStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
public class FluxFeedController {

    @Autowired
    private FeedStorageService feedStorageService;

    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");


    @GetMapping(value = "/feed-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Feed> streamEvents(@RequestParam(value = "from", required = false) String fromParam) {


        final LocalDateTime from = StringUtils.isEmpty(fromParam) ? LocalDateTime.now() : LocalDateTime.parse(fromParam, formatter);


        final LocalDateTime[] lastUpdate = new LocalDateTime[]{from};
        return Flux.interval(Duration.ofSeconds(1))
                .flatMap(s -> {
                    final List<Feed> feeds;
                    if (s == 0) {
                        feeds = feedStorageService.getFeedsFrom(from);
                    } else {
                        feeds = feedStorageService.getFeedsAfter(lastUpdate[0]);
                    }
                    if (!CollectionUtils.isEmpty(feeds))
                        lastUpdate[0] = feeds.get(feeds.size() - 1).getDate();

                    return Flux.just(feeds.toArray(new Feed[0]));
                });
    }
}
