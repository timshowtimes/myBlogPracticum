package com.timshowtime.service;

import com.timshowtime.model.Post;
import com.timshowtime.repository.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PostServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Post savePost(Post post) {
        String sql = "INSERT INTO post (title, tags, text, image) values (?, ?, ?, ?) RETURNING id";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getTagsAsText());
            ps.setString(3, post.getText());
            ps.setBytes(4, post.getImage());
            return ps;
        }, keyHolder);

        return findById(getKey(keyHolder).longValue());
    }

    @Override
    public Post updatePost(Post post) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "UPDATE post SET title = ?, tags = ?, text = ?, image = ? WHERE id = ? RETURNING id";

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getTagsAsText());
            ps.setString(3, post.getText());
            ps.setBytes(4, post.getImage());
            ps.setLong(5, post.getId());
            return ps;
        }, keyHolder);

        return findById(getKey(keyHolder).longValue());
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM post WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    private Number getKey(KeyHolder keyHolder) {
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new RuntimeException("Failed to save post: id was not generated");
        }
        return key;
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
    public List<Post> findAll(Pageable pageable, String tag) {
        StringBuilder sql = new StringBuilder("SELECT * FROM post");
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
    public Post findById(Long id) {
        String sql = "SELECT * FROM post WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Post.class), id);
    }
}
