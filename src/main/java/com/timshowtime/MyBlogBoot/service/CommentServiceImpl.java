package com.timshowtime.MyBlogBoot.service;

import com.timshowtime.MyBlogBoot.model.Comment;
import com.timshowtime.MyBlogBoot.repository.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;


@Service
public class CommentServiceImpl implements CommentService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public CommentServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addComment(Comment comment) {
        String sql = "INSERT INTO comment(text, post_id) VALUES (?,?)";

        jdbcTemplate.update(
                sql,
                comment.getText(),
                comment.getPostId()
        );
    }

    @Override
    public void updateComment(Comment comment) {
        String sql = "UPDATE comment SET text = ?, post_id = ? WHERE id = ?";

        jdbcTemplate.update(
                sql,
                comment.getText(),
                comment.getPostId(),
                comment.getId()
        );
    }

    @Override
    public void deleteComment(long postId, long commentId) {
        String sql = "DELETE FROM comment WHERE post_id = ? and id = ?";
        jdbcTemplate.update(sql, postId, commentId);
    }

    @Override
    public Comment findById(long id) {
        String sql = "SELECT * FROM comment WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Comment.class), id);
    }
}
