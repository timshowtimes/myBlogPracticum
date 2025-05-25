package com.timshowtime.MyBlogBoot.repository;

import com.timshowtime.MyBlogBoot.exception.PostNotFoundException;
import com.timshowtime.MyBlogBoot.model.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

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
