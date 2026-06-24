package com.homehunt.config;

import com.homehunt.entity.Property;
import com.homehunt.entity.PropertyStatus;
import com.homehunt.entity.PropertyType;
import com.homehunt.entity.Role;
import com.homehunt.entity.User;
import com.homehunt.repository.PropertyRepository;
import com.homehunt.repository.UserRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DataSeeder {
    @Bean
    CommandLineRunner seed(UserRepository users, PropertyRepository properties, PasswordEncoder encoder) {
        return args -> {
            if (users.count() > 0) {
                return;
            }
            User landlord = users.save(User.builder()
                    .name("Maya Chen")
                    .email("landlord@homehunt.dev")
                    .password(encoder.encode("password123"))
                    .role(Role.LANDLORD)
                    .build());
            users.save(User.builder()
                    .name("Taylor Reed")
                    .email("tenant@homehunt.dev")
                    .password(encoder.encode("password123"))
                    .role(Role.TENANT)
                    .build());
            users.save(User.builder()
                    .name("Admin User")
                    .email("admin@homehunt.dev")
                    .password(encoder.encode("password123"))
                    .role(Role.ADMIN)
                    .build());
            properties.save(Property.builder()
                    .title("Sunny Two Bedroom Apartment")
                    .description("Bright city apartment near transit, cafes, and parks.")
                    .location("Sydney NSW")
                    .price(new BigDecimal("780"))
                    .type(PropertyType.APARTMENT)
                    .imageUrl("https://images.unsplash.com/photo-1522708323590-d24dbb6b0267")
                    .status(PropertyStatus.APPROVED)
                    .owner(landlord)
                    .build());
        };
    }
}
