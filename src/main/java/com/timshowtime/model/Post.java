package com.timshowtime.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Data
@NoArgsConstructor
public class Post {
    private long id;
    private String title;
    private String tags;
    private String text;
    private byte[] image;
    private long likesCount;
    private List<Comment> comments;
    private int nWord = 30;

    public String getTextPreview() {
        if (text == null || text.isEmpty()) return "";
        String[] words = text.split("\\s+");
        if (words.length <= nWord) {
            return text.trim();
        }
        return String.join(" ", Arrays.copyOfRange(words, 0, nWord)) + "...";
    }

    public String[] getTextParts() {
        return text.split("\n");
    }

    public String getTagsAsText() {
        return String.join("#", tags);
    }

    public List<String> getTags() {
        return Arrays.stream(tags.split("#"))
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toList());
    }
}
