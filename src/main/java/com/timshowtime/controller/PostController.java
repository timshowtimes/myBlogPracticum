package com.timshowtime.controller;

import com.timshowtime.model.Post;
import com.timshowtime.model.PostPageable;
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

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
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

    private PostPageable getPostPageable(Integer pageNumber, Integer pageSize) {
        Long count = postService.count();
        return new PostPageable(pageNumber, pageSize, count);
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

        Post post = new Post();
        post.setTitle(title);
        post.setText(text);
        post.setTags(tags);
        post.setText(text);
        post.setImage(image.getBytes());

        Post newPost = postService.savePost(post);
        System.out.println("New Post id: " + newPost.getId());
        return "redirect:/posts";
    }
}
