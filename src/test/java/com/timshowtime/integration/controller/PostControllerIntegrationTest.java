package com.timshowtime.integration.controller;

import com.timshowtime.config.test.web.TestWebConfiguration;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringJUnitConfig(classes = {TestWebConfiguration.class})
@WebAppConfiguration
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class PostControllerIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("test_post_db")
            .withUsername("testuser")
            .withPassword("testpass");

    static {
        postgresContainer.start();
    }

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @DynamicPropertySource // происходит до поднятия контекста (регистрирует тестовые значения)
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }

    @BeforeAll
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @BeforeEach
    public void before() {
        jdbcTemplate.update("TRUNCATE TABLE comment RESTART IDENTITY CASCADE");
        jdbcTemplate.update("TRUNCATE TABLE post RESTART IDENTITY CASCADE");

        jdbcTemplate.update(
                "INSERT INTO post (title, tags, text, image) VALUES (?, ?, ?, ?)",
                "First Post", "#java #spring", "This is the text of the FIRST post", null
        );

        jdbcTemplate.update(
                "INSERT INTO post (title, tags, text, image) VALUES (?, ?, ?, ?)",
                "Second Post", "#testcontainers #database", "This is the text of the SECOND post", null
        );
    }

    @Test
    void getPosts() throws Exception {
        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("First Post")))
                .andExpect(content().string(containsString("Second Post")));
    }

    @Test
    void createPost() throws Exception {
        String NEW_TEXT = "Some NEW text content";

        mockMvc.perform(multipart("/posts/upload")
                        .file(provideMultipartFile())
                        .param("title", "someTitle")
                        .param("tags", "#tag1 #tag2")
                        .param("text", NEW_TEXT))
                .andExpect(status().is3xxRedirection()).
                andExpect(redirectedUrl("/posts"));

        getPostByIdAndCompareText(3, NEW_TEXT); // check CREATED post
    }

    @Test
    void updatePost() throws Exception {
        String UPDATE_TEXT = "Update text content for FIRST post";

        // check current text for the FIRST post
        getPostByIdAndCompareText(1, "This is the text of the FIRST post");

        mockMvc.perform(multipart("/posts/1")
                        .file(provideMultipartFile())
                        .param("title", "updateTitle")
                        .param("tags", "#updateTag1,updateTag2")
                        .param("text", UPDATE_TEXT))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        // check UPDATED post
        getPostByIdAndCompareText(1, UPDATE_TEXT);
    }

    @Test
    void deletePost() throws Exception {
        mockMvc.perform(get("/posts/" + 1))
                .andExpect(status().isOk()); // check that Post 1 is existed

        mockMvc.perform(post("/posts/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        mockMvc.perform(get("/posts/" + 1))
                .andExpect(status().isNotFound()); // check that Post 1 was deleted
    }

    @Test
    void setLikePost() throws Exception {
        int likeCount = 0;
        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        "<span id=\"likesCount\">" + likeCount + "</span>")
                ));

        mockMvc.perform(post("/posts/1/like")
                        .param("like", "true"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        "<span id=\"likesCount\">" + (likeCount + 1) + "</span>")
                ));
    }

    @Test
    void setCommentPost() throws Exception {
        setCommentAndVerify();
    }

    @Test
    void updateCommentPost() throws Exception {
        setCommentAndVerify();

        String UPDATED_TEXT = "Updated comment text for Post 1";
        mockMvc.perform(post("/posts/1/comments/1")
                        .param("text", UPDATED_TEXT))
                .andExpect(status().is3xxRedirection());
        getPostByIdAndCompareText(1, UPDATED_TEXT);
    }

    private void setCommentAndVerify() throws Exception {
        String COMMENT_TEXT = "Comment text for Post 1";
        mockMvc.perform(post("/posts/1/comments")
                        .param("text", COMMENT_TEXT))
                .andExpect(status().is3xxRedirection());
        getPostByIdAndCompareText(1, COMMENT_TEXT);
    }

    private MockMultipartFile provideMultipartFile() {
        return new MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                "some image content".getBytes()
        );
    }

    private void getPostByIdAndCompareText(long id, String text) throws Exception {
        mockMvc.perform(get("/posts/" + id))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString(text)));
    }

}

