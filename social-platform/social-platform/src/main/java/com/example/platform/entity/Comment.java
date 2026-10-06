package com.example.platform.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "comments")
public class Comment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String text;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id")
    private Post post;

    public Comment() {}
    public Comment(String text, Post post) { this.text = text; this.post = post; }
    public Long getId() { return id; }
    public String getText() { return text; }
}
