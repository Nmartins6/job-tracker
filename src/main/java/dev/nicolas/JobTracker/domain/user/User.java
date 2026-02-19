package dev.nicolas.JobTracker.domain.user;

import dev.nicolas.JobTracker.domain.shared.exception.DomainException;

import java.util.UUID;

public class User {

    private UUID id;
    private String name;
    private String email;
    private String passwordHash;
    private String headLine;
    private String location;
    private String bio;

    private User() {

    }

    public static User create(String name, String email, String passwordHash, String headLine, String location, String bio) {
        if (name == null || name.isBlank()) {
            throw new DomainException("Nome de usuário não pode ser vazio");
        }
        if (email == null || email.isBlank()) {
            throw new DomainException("Email não pode ser vazio");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new DomainException("Senha não pode ser vazia");
        }

        User user = new User();
        user.id = UUID.randomUUID();
        user.name = name.trim();
        user.email = email.trim().toLowerCase();
        user.passwordHash = passwordHash;
        user.headLine = headLine;
        user.location = location;
        user.bio = bio;

        return user;
    }

    public static User reconstitute(UUID id, String name, String email, String passwordHash, String headLine, String location, String bio) {

        User user = new User();
        user.id = id;
        user.name = name;
        user.email = email;
        user.passwordHash = passwordHash;
        user.headLine = headLine;
        user.location = location;
        user.bio = bio;

        return user;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getHeadLine() {
        return headLine;
    }

    public String getLocation() {
        return location;
    }

    public String getBio() {
        return bio;
    }
}
