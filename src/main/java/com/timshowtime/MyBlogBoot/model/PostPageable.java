package com.timshowtime.MyBlogBoot.model;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public class PostPageable {
    private int pageNumber;
    private int pageSize;
    private long totalPages;

    public int pageNumber() {
        return pageNumber;
    }

    public int pageSize() {
        return pageSize;
    }

    public boolean hasPrevious() {
        return pageNumber + 1 > 1;
    }

    public boolean hasNext() {
        int nextPage = (pageNumber + 1) * pageSize;
        return nextPage < totalPages;
    }
}
