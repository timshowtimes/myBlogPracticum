package com.timshowtime.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class Post {
    private long id;
    @NotBlank(message = "Title should not be empty")
    private String title;
    private String tags;
    private String text;
    private byte[] image;
    private long likesCount;
    private List<Comment> comments;

    public String getTextPreview() {
        return text;
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
