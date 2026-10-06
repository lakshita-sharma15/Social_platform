package com.example.platform.repository;

import com.example.platform.entity.Post;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    // Pagination + JOIN FETCH on author: avoids N+1 when mapping author name
    @Query(value = "SELECT p FROM Post p JOIN FETCH p.author WHERE p.status = :status",
           countQuery = "SELECT COUNT(p) FROM Post p WHERE p.status = :status")
    Page<Post> findByStatusWithAuthor(@Param("status") String status, Pageable pageable);

    // JPQL by author (Exp 6 - JPQL example)
    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.author.id = :authorId ORDER BY p.createdAt DESC")
    List<Post> findByAuthorId(@Param("authorId") Long authorId);

    // N+1 fix: posts + comments in a single query
    @Query("SELECT DISTINCT p FROM Post p JOIN FETCH p.author LEFT JOIN FETCH p.comments WHERE p.status = 'PUBLISHED'")
    List<Post> findAllWithComments();

    // Native query for top-performing posts
    @Query(value = "SELECT * FROM posts WHERE status = 'PUBLISHED' ORDER BY likes DESC LIMIT 5", nativeQuery = true)
    List<Post> findTopPosts();

    // Posts due for publishing (scheduler)
    @Query("SELECT p FROM Post p WHERE p.status = 'SCHEDULED' AND p.scheduledAt <= :now")
    List<Post> findDue(@Param("now") LocalDateTime now);

    long countByStatus(String status);

    @Query("SELECT COALESCE(SUM(p.likes), 0) FROM Post p WHERE p.status = 'PUBLISHED'")
    long sumLikes();
}
