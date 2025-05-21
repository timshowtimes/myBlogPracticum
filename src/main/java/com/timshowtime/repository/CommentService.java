package com.timshowtime.repository;


import com.timshowtime.model.Comment;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentService {
    Comment addComment(Comment comment);
    Comment findById(long id);
    Comment updateComment(Comment comment);
    void deleteComment(long postId, long commentId);
}
