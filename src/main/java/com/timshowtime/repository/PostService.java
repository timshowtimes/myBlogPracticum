package com.timshowtime.repository;

import com.timshowtime.exception.PostNotFoundException;
import com.timshowtime.model.Post;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface PostService {
    void savePost(Post post);
    void updatePost(Post post);
    void deleteById(Long id);
    List<Post> findAll(Pageable pageable, String tag);
    Post findById(Long id) throws PostNotFoundException;
    Long count();
    byte[] getImageByPostId(Long postId);
    void setLike(Long postId, Boolean like);
}
