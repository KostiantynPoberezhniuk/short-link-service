package org.example.urlshortener.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {

    @Test
    void settersAndGettersWork() {
        User user = new User("alice", "pwd", Role.USER);
        user.setUsername("bob");
        user.setPassword("secret");
        user.setRole(Role.ADMIN);

        assertThat(user.getUsername()).isEqualTo("bob");
        assertThat(user.getPassword()).isEqualTo("secret");
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        assertThat(user.getCreatedAt()).isNotNull();
    }

    @Test
    void equalsAndHashCodeForTransientEntities() {
        User user = new User("alice", "pwd", Role.USER);

        assertThat(user).isEqualTo(user);
        assertThat(user).isNotEqualTo(new Object());
        assertThat(user).isNotEqualTo(new User("alice", "pwd", Role.USER));
        assertThat(user.hashCode()).isEqualTo(User.class.hashCode());
    }
}
