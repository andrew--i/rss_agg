package org.ai.storage;

import org.ai.model.Feed;

import java.time.LocalDateTime;
import java.util.List;

public interface FeedStorageService {

    String getResourcePath(String feedUrl);

    void complete(Feed item);

    void fail(String feedUrl, String message);

    void cancel(String feedUrl);

    List<Feed> getFeedsAfter(LocalDateTime date);

    List<Feed> getFeedsFrom(LocalDateTime date);

}
