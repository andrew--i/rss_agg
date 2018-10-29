package org.ai.view;

import com.rometools.rome.feed.rss.Channel;
import com.rometools.rome.feed.rss.Item;
import org.ai.model.Feed;
import org.ai.storage.FeedStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.feed.AbstractRssFeedView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FeedView extends AbstractRssFeedView {

    private final FeedStorageService feedStorageService;

    @Autowired
    public FeedView(FeedStorageService feedStorageService) {
        setContentType(MediaType.APPLICATION_RSS_XML_VALUE.concat(";charset=UTF-8"));
        this.feedStorageService = feedStorageService;
    }

    @Override
    protected void buildFeedMetadata(Map<String, Object> model, Channel feed, HttpServletRequest request) {
        feed.setTitle("Аггрегатор подписок");
        feed.setDescription("Позволяет склеить новостные ленты в одну");
        feed.setLink(request.getRequestURL().toString());
    }


    @Override
    protected List<Item> buildFeedItems(Map<String, Object> map, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {

        final List<Feed> feeds = feedStorageService.getFeeds();
        return feeds.stream()
                .map(f -> {
                    final Item item = new Item();
                    item.setTitle(f.getTitle());
                    item.setPubDate(f.getDate());
                    item.setLink(f.getLink());
                    return item;
                    }).collect(Collectors.toList());
    }
}
