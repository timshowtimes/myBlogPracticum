package com.timshowtime.service;

import com.timshowtime.model.Comment;
import com.timshowtime.repository.CommentService;
import com.timshowtime.util.SqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;

@Service
public class CommentServiceImpl implements CommentService, SqlUtils {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public CommentServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Comment addComment(Comment comment) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO comment(text, post_id) VALUES (?,?) RETURNING id";

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, comment.getText());
            ps.setLong(2, comment.getPostId());
            return ps;
        }, keyHolder);

        return findById(getKey(keyHolder).longValue());

    }

    @Override
    public Comment updateComment(Comment comment) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "UPDATE comment SET text = ?, post_id = ? WHERE id = ? RETURNING id";
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, comment.getText());
            ps.setLong(2, comment.getPostId());
            ps.setLong(3, comment.getId());
            return ps;
        }, keyHolder);

        return findById(getKey(keyHolder).longValue());
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
