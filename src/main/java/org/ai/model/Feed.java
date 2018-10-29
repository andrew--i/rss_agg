package org.ai.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Feed {
    private String src;
    private LocalDateTime date;
    private String title;
    private String link;
    private String storagePath;

    public void setSrc(String src) {
        this.src = src;
    }

    public String getSrc() {
        return src;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getStoragePath() {
        return storagePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Feed feed = (Feed) o;
        return Objects.equals(src, feed.src) &&
                Objects.equals(date, feed.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(src, date);
    }
}
