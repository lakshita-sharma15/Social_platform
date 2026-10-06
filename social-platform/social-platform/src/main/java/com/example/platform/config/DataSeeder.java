package com.example.platform.config;

import com.example.platform.entity.Comment;
import com.example.platform.entity.Post;
import com.example.platform.entity.User;
import com.example.platform.repository.PostRepository;
import com.example.platform.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Random;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Demo data: admin/admin123, user/user123 and 25 posts so pagination can be tested. */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(UserRepository users, PostRepository posts, PasswordEncoder encoder) {
        return args -> {
            User admin = users.save(new User("admin", encoder.encode("admin123"), "ROLE_ADMIN"));
            User user = users.save(new User("user", encoder.encode("user123"), "ROLE_USER"));
            Random rnd = new Random(42);
            for (int i = 1; i <= 25; i++) {
                Post p = new Post();
                p.setContent("Sample post #" + i);
                p.setAuthor(i % 2 == 0 ? admin : user);
                p.setLikes(rnd.nextInt(500));
                p.setCreatedAt(LocalDateTime.now().minusMinutes(i));
                for (int c = 0; c < 3; c++) p.getComments().add(new Comment("Comment " + c + " on post " + i, p));
                posts.save(p);
            }
        };
    }
}
