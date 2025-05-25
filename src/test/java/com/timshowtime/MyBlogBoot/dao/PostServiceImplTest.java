package com.timshowtime.MyBlogBoot.dao;

import com.timshowtime.MyBlogBoot.exception.PostNotFoundException;
import com.timshowtime.MyBlogBoot.model.Post;
import com.timshowtime.MyBlogBoot.service.PostServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public class PostServiceImplTest {

    @Container
    public static PostgreSQLContainer<?> psqlContainer = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("data_jdbc_test_db")
            .withUsername("testuser")
            .withPassword("testpass");

    static {
        psqlContainer.start();
    }

    @Autowired
    private PostServiceImpl postService;


    @Autowired
    private JdbcTemplate jdbcTemplate;


    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", psqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", psqlContainer::getUsername);
        registry.add("spring.datasource.password", psqlContainer::getPassword);
    }


    @BeforeEach
    public void refreshDb() {
        jdbcTemplate.update("TRUNCATE TABLE comment RESTART IDENTITY CASCADE");
        jdbcTemplate.update("TRUNCATE TABLE post RESTART IDENTITY CASCADE");

        jdbcTemplate.update(
                "INSERT INTO post (title, tags, text, image) VALUES (?, ?, ?, ?)",
                "First Post Title", "#java #spring", "This is the text of the FIRST post", null
        );

        jdbcTemplate.update(
                "INSERT INTO post (title, tags, text, image) VALUES (?, ?, ?, ?)",
                "Second Post Title", "#testcontainers #database", "This is the text of the SECOND post", null
        );
    }


    @Test
    void getAllPosts() {
        var postList = postService.findAll(PageRequest.of(0, 5), null);

        assertNotNull(postList);
        assertFalse(postList.isEmpty());
        assertEquals(2, postList.size());
        assertTrue(postList.stream().anyMatch(post -> post.getTitle().equals("First Post Title")));
    }

    @Test
    void createPost() {
        Post post = provideDefaultPost();

        postService.savePost(post);

        Post saved = postService.findById(3L);

        assertNotNull(saved);
        assertEquals(post.getTitle(), saved.getTitle());
        assertEquals(post.getTagsAsText(), saved.getTagsAsText());
        assertEquals(post.getText(), saved.getText());
    }


    @Test
    void deletePost() {
        Post post = postService.findById(1L);

        assertNotNull(post);

        postService.deleteById(post.getId());

        assertThrows(PostNotFoundException.class, () -> postService.findById(1L));
    }

    @Test
    void updatePost() {
        Post post = postService.findById(1L);

        assertEquals("First Post Title", post.getTitle());

        post.setTitle("Updated Post Title");

        postService.updatePost(post);

        Post updated = postService.findById(1L);
        assertNotNull(updated);
        assertEquals("Updated Post Title", updated.getTitle());
    }

    @Test
    void setLikePost() {
        int likes = 0;
        Post post = postService.findById(1L);

        assertEquals(likes, post.getLikesCount());

        postService.setLike(1L, true);

        Post likedPost = postService.findById(1L);

        assertEquals((likes + 1), likedPost.getLikesCount());
    }


    private Post provideDefaultPost() {
        Post post = new Post();
        post.setTitle("My Title");
        post.setTags("tag1 tag2");
        post.setText("Some post content");
        post.setImage(new byte[]{1, 2, 3, 4});
        return post;
    }
}
