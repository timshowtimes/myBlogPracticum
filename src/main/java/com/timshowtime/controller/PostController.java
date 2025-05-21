package com.timshowtime.controller;

import com.timshowtime.model.Comment;
import com.timshowtime.model.Post;
import com.timshowtime.model.PostPageable;
import com.timshowtime.repository.CommentService;
import com.timshowtime.repository.PostService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final CommentService commentService;

    @Autowired
    public PostController(PostService postService, CommentService commentService) {
        this.postService = postService;
        this.commentService = commentService;
    }

    @GetMapping
    public String posts(@RequestParam(value = "search", required = false) String tag,
                        @RequestParam(value = "pageSize", required = false, defaultValue = "5") Integer pageSize,
                        @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
                        Model model) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        PostPageable paging = getPostPageable(pageable.getPageNumber(), pageable.getPageSize());

        List<Post> posts = postService.findAll(pageable, tag);
        model.addAttribute("posts", posts);
        model.addAttribute("search", tag);
        model.addAttribute("paging", paging);
        return "posts";
    }

    @GetMapping("/{id}")
    public String getPostPage(@PathVariable("id") Long id, Model model) {
        model.addAttribute("post", postService.findById(id));
        return "post";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable("id") Long id, Model model) {
        model.addAttribute("post", postService.findById(id));
        return "add-post";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id) {
        postService.deleteById(id);
        System.out.println("Post with id " + id + " successfully deleted.");
        return "redirect:/posts";
    }

    @GetMapping("/images/{id}")
    public void getPostImage(@PathVariable("id") Long postId, HttpServletResponse response) throws IOException {
        byte[] image = postService.getImageByPostId(postId);

        if (image == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType("image/jpeg");
        response.setContentLength(image.length);
        response.getOutputStream().write(image);
        response.getOutputStream().flush();
    }

    @GetMapping("/add")
    public String addPostPage() {
        return "add-post";
    }

    @PostMapping("/upload")
    public String addPost(@RequestParam("image") MultipartFile image,
                          @RequestParam("title") String title,
                          @RequestParam("tags") String tags,
                          @RequestParam("text") String text) throws IOException {

        Post post = Post.builder()
                .title(title)
                .text(text)
                .tags(tags)
                .image(image.getBytes())
                .build();

        postService.savePost(post);
//        System.out.println("New Post id: " + newPost.getId());
        return "redirect:/posts";
    }

    @PostMapping("/{id}/like")
    public String addPostLike(@PathVariable("id") Long postId,
                              @RequestParam("like") Boolean like) {
        postService.setLike(postId, like);
        System.out.println("Post with id " + postId + " successfully liked.");
        return "redirect:/posts/" + postId;
    }

    @PostMapping("/{id}/comments")
    public String addPostComment(@PathVariable("id") Long postId,
                                 @RequestParam("text") String text) {
        Comment comment = Comment.builder()
                .text(text)
                .postId(postId)
                .build();
        commentService.addComment(comment);
        return "redirect:/posts/" + postId;
    }

    @PostMapping("/{id}/comments/{comId}")
    public String updatePostComment(@PathVariable("id") Long postId,
                                    @PathVariable("comId") Long comID,
                                    @RequestParam("text") String text) {
        Comment comment = commentService.findById(comID);

        if (comment == null) {
            throw new RuntimeException("Comment with id " + comID + " not found.");
        }

        comment.setText(text);
        comment.setPostId(postId);
        commentService.updateComment(comment);
        return "redirect:/posts/" + postId;
    }

    @PostMapping("/{postId}/comments/{comId}/delete")
    public String deletePostComment(@PathVariable("postId") Long postId,
                                    @PathVariable("comId") Long comID) {
        if (postId == null || comID == null) {
            throw new RuntimeException("Comment with id " + comID + " or post with id " + postId + " not found.");
        }

        commentService.deleteComment(postId, comID);
        return "redirect:/posts/" + postId;
    }


    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id,
                         @RequestParam("image") MultipartFile image,
                         @RequestParam("title") String title,
                         @RequestParam("tags") String tags,
                         @RequestParam("text") String text) throws IOException {
        Post post = postService.findById(id);
        if (post == null) {
            throw new RuntimeException("Post with id " + id + " not found");
        }

        post.setTitle(title);
        post.setText(text);
        post.setTags(tags);
        if (image.getBytes().length != 0) post.setImage(image.getBytes());

        postService.updatePost(post);
        System.out.println("Post with id " + id + " successfully updated.");
        return "redirect:/posts";
    }

    private PostPageable getPostPageable(Integer pageNumber, Integer pageSize) {
        Long count = postService.count();
        return new PostPageable(pageNumber, pageSize, count);
    }
}
