package com.timshowtime.service;

import com.timshowtime.exception.PostNotFoundException;
import com.timshowtime.model.Comment;
import com.timshowtime.model.Post;
import com.timshowtime.repository.PostService;
import com.timshowtime.util.SqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostServiceImpl implements PostService, SqlUtils {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PostServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void savePost(Post post) {
        String sql = "INSERT INTO post (title, tags, text, image) values (?, ?, ?, ?)";

        jdbcTemplate.update(
                sql,
                post.getTitle(),
                post.getTagsAsText(),
                post.getText(),
                post.getImage()
        );

    }

    @Override
    public void updatePost(Post post) {
        String sql = "UPDATE post SET title = ?, tags = ?, text = ?, image = ? WHERE id = ?";

        jdbcTemplate.update(
                sql,
                post.getTitle(),
                post.getTagsAsText(),
                post.getText(),
                post.getImage(),
                post.getId()
        );
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM post WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Long count() {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM post", Long.class);
    }

    @Override
    public byte[] getImageByPostId(Long postId) {
        String sql = "SELECT image FROM post WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{postId}, byte[].class);
    }

    @Override
    public void setLike(Long postId, Boolean like) {
        String sql = "UPDATE post SET likes_count = likes_count " + (like ? "+ 1 " : "- 1 ") +  "WHERE id = ?";
        jdbcTemplate.update(sql, postId);
    }


    @Override
    public List<Post> findAll(Pageable pageable, String tag) {
        StringBuilder sql = new StringBuilder("SELECT p.*, coalesce(pc.comments_count, 0) as comments_count\n" +
                "FROM post p\n" +
                "LEFT JOIN (SELECT post_id, COUNT(*) AS comments_count\n" +
                "           FROM comment\n" +
                "           GROUP BY post_id) pc ON p.id = pc.post_id"); // подсчет комментов для каждого поста
        List<Object> params = new ArrayList<>();

        if (tag != null && !tag.isEmpty()) {
            sql.append(" WHERE tags ILIKE ?");
            params.add("%" + tag + "%");
        }

        sql.append(" ORDER BY created_at DESC OFFSET ? LIMIT ?");
        params.add(pageable.getOffset());
        params.add(pageable.getPageSize());

        return jdbcTemplate.query(
                sql.toString(),
                new BeanPropertyRowMapper<>(Post.class),
                params.toArray()
        );
    }

    @Override
    public Post findById(Long id) throws PostNotFoundException {
        String postSql = "SELECT * FROM post WHERE id = ?";
        String commentSql = "SELECT * FROM comment WHERE post_id = ? ORDER BY created_at DESC";

        try {
            Post post = jdbcTemplate.queryForObject(postSql, new BeanPropertyRowMapper<>(Post.class), id);
            List<Comment> comments = jdbcTemplate.query(commentSql, new BeanPropertyRowMapper<>(Comment.class), id);
            if (post != null && !comments.isEmpty()) {
                post.setComments(comments);
            }
            return post;
        } catch (EmptyResultDataAccessException e) {
            throw new PostNotFoundException(id);
        }
    }
}
