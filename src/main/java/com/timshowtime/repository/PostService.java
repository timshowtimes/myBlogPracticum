package com.timshowtime.repository;

import com.timshowtime.model.Post;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface PostService {
    Post savePost(Post post);
    List<Post> findAll(Pageable pageable, String tag);
    Long count();
    byte[] getImageByPostId(Long postId);
}
