package com.timshowtime.unit.service;

import com.timshowtime.exception.PostNotFoundException;
import com.timshowtime.model.Post;
import com.timshowtime.service.PostServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class PostServiceImplTest {

    private JdbcTemplate jdbcTemplate;
    private PostServiceImpl postService;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        postService = new PostServiceImpl(jdbcTemplate);
    }

    @Test
    void savePost() {
        Post post = providePostObject();

        postService.savePost(post);

        verify(jdbcTemplate, times(1))
                .update("INSERT INTO post (title, tags, text, image) values (?, ?, ?, ?)",
                        post.getTitle(), post.getTagsAsText(), post.getText(), post.getImage());
    }

    @Test
    void deletePost() {
        postService.deleteById(1L);
        verify(jdbcTemplate, times(1)).update("DELETE FROM post WHERE id = ?", 1L);
    }

    @Test
    void countPosts() {
        when(jdbcTemplate.queryForObject("SELECT count(*) FROM post", Long.class)).thenReturn(5L);
        assertEquals(5L, postService.count());
    }

    @Test
    void getImageByPostId() {
        byte[] image = new byte[]{1, 2, 3, 4};
        when(jdbcTemplate.queryForObject("SELECT image FROM post WHERE id = ?",  new Object[]{1L}, byte[].class))
                .thenReturn(image);

        byte[] result = postService.getImageByPostId(1L);
        assertEquals(image, result);
    }

    @Test
    void setLike() {
        postService.setLike(1L, true);
        verify(jdbcTemplate).update("UPDATE post SET likes_count = likes_count + 1 WHERE id = ?", 1L);
    }

    @Test
    void setUnlike() {
        postService.setLike(1L, false);
        verify(jdbcTemplate).update("UPDATE post SET likes_count = likes_count - 1 WHERE id = ?", 1L);
    }

    @Test
    void findByIdNotFound() {
        when(jdbcTemplate.queryForObject(anyString(), any(BeanPropertyRowMapper.class), anyLong()))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThrows(PostNotFoundException.class, () -> postService.findById(1L));
    }

    @Test
    void findAllWithTag() {
        Post post = providePostObject();

        when(jdbcTemplate.query(anyString(), any(BeanPropertyRowMapper.class), any(Object[].class)))
                .thenReturn(Collections.singletonList(post));

        List<Post> postList = postService.findAll(PageRequest.of(0, 5), "tag1");

        assertEquals(1, postList.size());
        assertEquals(post, postList.get(0));
    }

    private Post providePostObject() {
        Post post = new Post();
        post.setTitle("My Title");
        post.setTags("tag1 tag2");
        post.setText("Some post content");
        post.setImage(new byte[]{1, 2, 3, 4});
        return post;
    }
}
