package com.timshowtime.MyBlogBoot.repository;


import com.timshowtime.MyBlogBoot.model.Comment;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentService {
    void addComment(Comment comment);
    Comment findById(long id);
    void updateComment(Comment comment);
    void deleteComment(long postId, long commentId);
}
