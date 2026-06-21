package ru.bookstore.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.bookstore.domain.User;
import ru.bookstore.repository.postgres.impl.UserRepositoryImpl;
import ru.bookstore.repository.postgres.mapper.UserEntityMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("postgres")
@Import({UserRepositoryImpl.class, UserEntityMapper.class})
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndFindByIdShouldWork() {
        User saved = userRepository.save(buildUser("reader1", "reader1@test.local"));

        Optional<User> found = userRepository.findById(saved.getUserId());

        assertTrue(found.isPresent());
        assertEquals("reader1", found.get().getUsername());
        assertEquals("reader1@test.local", found.get().getEmail());
    }

    @Test
    void findersAndExistsShouldWork() {
        userRepository.save(buildUser("reader2", "reader2@test.local"));

        Optional<User> byEmail = userRepository.findByEmail("reader2@test.local");
        Optional<User> byUsername = userRepository.findByUsername("reader2");

        assertTrue(byEmail.isPresent());
        assertTrue(byUsername.isPresent());
        assertTrue(userRepository.existsByEmail("reader2@test.local"));
        assertTrue(userRepository.existsByUsername("reader2"));
        assertTrue(userRepository.existsById(byEmail.get().getUserId()));
    }

    @Test
    void countByRoleIgnoreCaseWorks() {
        User a1 = new User();
        a1.setUsername("adm1");
        a1.setEmail("adm1@test.local");
        a1.setPasswordHash("h");
        a1.setRole("ADMIN");
        userRepository.save(a1);

        User a2 = new User();
        a2.setUsername("adm2");
        a2.setEmail("adm2@test.local");
        a2.setPasswordHash("h");
        a2.setRole("admin");
        userRepository.save(a2);

        assertEquals(2, userRepository.countByRole("ADMIN"));
    }

    @Test
    void findAllCountDeleteShouldWork() {
        User u1 = userRepository.save(buildUser("reader3", "reader3@test.local"));
        userRepository.save(buildUser("reader4", "reader4@test.local"));

        List<User> all = userRepository.findAll();
        assertEquals(2, all.size());
        assertEquals(2, userRepository.count());

        userRepository.deleteById(u1.getUserId());
        assertFalse(userRepository.existsById(u1.getUserId()));
        assertEquals(1, userRepository.count());
    }

    private User buildUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash("hash");
        user.setRole("CUSTOMER");
        return user;
    }
}
