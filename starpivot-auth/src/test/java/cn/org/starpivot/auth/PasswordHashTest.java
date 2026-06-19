package cn.org.starpivot.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHashTest {

    private static final String ADMIN_PASSWORD_HASH =
            "$2a$10$qkb8F/erWiJT9D2/ouAkk.PXJOTTQ/UxVqrWq6DL9k3/ealix1dui";

    @Test
    void adminPasswordMatches() {
        assertTrue(new BCryptPasswordEncoder().matches("123456", ADMIN_PASSWORD_HASH));
    }
}
