package com.timshowtime.MyBlogBoot.mvc;

import com.timshowtime.MyBlogBoot.controller.PostController;
import com.timshowtime.MyBlogBoot.model.Comment;
import com.timshowtime.MyBlogBoot.model.Post;
import com.timshowtime.MyBlogBoot.repository.CommentService;
import com.timshowtime.MyBlogBoot.repository.PostService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockReset;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PostController.class)
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean(reset = MockReset.BEFORE)
    private PostService postService;

    @MockitoBean(reset = MockReset.BEFORE)
    private CommentService commentService;

    @Test
    void getAllPosts() throws Exception {
        when(postService.findAll(any(Pageable.class), nullable(String.class)))
                .thenReturn(List.of(
                        createPost(1L, "Title1", "Text For Post 1", 5L),
                        createPost(2L, "Title2", "Text For Post 2", 3L),
                        createPost(3L, "Title3", "Text For Post 3", 10L)
                ));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(model().attributeExists("posts"))
                .andExpect(view().name("posts"))
                .andExpect(content().string(containsString("Text For Post 1")))
                .andExpect(content().string(containsString("Text For Post 2")))
                .andExpect(content().string(containsString("Text For Post 3")));

        verify(postService, times(1)).findAll(any(Pageable.class), nullable(String.class));
    }


    @Test
    void createPost() throws Exception {
        Post post = createPost(1L, "Title1", "Text For Created Post 1", 5L);
        postService.savePost(post);

        mockMvc.perform(multipart("/posts/upload")
                .file(provideMultipartFile())
                .param("title", post.getTitle())
                .param("tags", post.getTagsAsText())
                .param("text", post.getText()));

        verify(postService, times(1)).savePost(post);
    }

    @Test
    void updatePost() throws Exception {
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);

        Post post = createPost(1L, "Title1", "Text For Update Post 1", 5L);
        Post updatePost = createPost(1L, "Update Title1", "Update Text for Update Post 1", 5L);

        when(postService.findById(post.getId())).thenReturn(post);

        mockMvc.perform(multipart("/posts/" + updatePost.getId())
                        .file(provideMultipartFile())
                        .param("title", updatePost.getTitle())
                        .param("tags", updatePost.getTagsAsText())
                        .param("text", updatePost.getText()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).updatePost(captor.capture());

        Post actualPost = captor.getValue();

        assertEquals(updatePost.getId(), actualPost.getId());
        assertEquals(updatePost.getTitle(), actualPost.getTitle());
        assertEquals(updatePost.getText(), actualPost.getText());
        assertEquals(updatePost.getTagsAsText(), actualPost.getTagsAsText());

    }

    @Test
    void deletePost() throws Exception {
        mockMvc.perform(post("/posts/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        verify(postService, times(1)).deleteById(1L);
    }

    @Test
    void setLikePost() throws Exception {
        mockMvc.perform(post("/posts/1/like").param("like", "true"));

        verify(postService, times(1)).setLike(1L, true);
    }

    @Test
    void setUnlikePost() throws Exception {
        mockMvc.perform(post("/posts/1/like").param("like", "false"));

        verify(postService, times(1)).setLike(1L, false);
    }

    @Test
    void setCommentPost() throws Exception {
        Comment comment = Comment.builder()
                .text("Test Comment Text")
                .postId(1L)
                .build();

        mockMvc.perform(post("/posts/1/comments")
                .param("text", comment.getText()));

        verify(commentService, times(1)).addComment(comment);
    }

    @Test
    void updateCommentPost() throws Exception {
        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);

        Comment oldComment = Comment.builder()
                .id(1L)
                .text("Old Comment Text")
                .postId(1L)
                .build();

        Comment newComment = Comment.builder()
                .id(1L)
                .text("New Comment Text")
                .postId(1L)
                .build();

        when(commentService.findById(oldComment.getId())).thenReturn(oldComment);

        mockMvc.perform(post("/posts/1/comments/" + oldComment.getId())
                        .param("text", newComment.getText()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1"));

        verify(commentService).updateComment(captor.capture());
        Comment actualComment = captor.getValue();

        assertEquals(newComment.getId(), actualComment.getId());
        assertEquals(newComment.getText(), actualComment.getText());
        assertEquals(newComment.getPostId(), actualComment.getPostId());
    }


    public Post createPost(Long id, String title, String text, long lc) {
        return Post.builder()
                .id(id)
                .title(title)
                .tags("#tag1#tag2")
                .text(text)
                .image(new byte[]{1, 2, 3})
                .likesCount(lc)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private MockMultipartFile provideMultipartFile() {
        return new MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                "some image content".getBytes()
        );
    }


}
