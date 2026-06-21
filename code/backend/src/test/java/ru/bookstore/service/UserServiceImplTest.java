package ru.bookstore.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.bookstore.domain.User;
import ru.bookstore.repository.UserRepository;
import ru.bookstore.service.exception.ResourceNotFoundException;
import ru.bookstore.service.exception.ValidationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void updateUserRoleSuccessfully() {
        User existing = new User(10L, "mod", "m@x", "hash", "CUSTOMER", null);
        when(userRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.updateUserRole(10L, "MODERATOR");

        assertEquals("MODERATOR", result.getRole());
        verify(userRepository).save(existing);
    }

    @Test
    void updateUserRoleInvalidRoleThrows() {
        assertThrows(ValidationException.class, () -> userService.updateUserRole(1L, "GOD"));
        assertThrows(ValidationException.class, () -> userService.updateUserRole(1L, "  "));
    }

    @Test
    void updateUserRoleUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUserRole(99L, "CUSTOMER"));
    }

    @Test
    void updateUserRoleDemoteLastAdminForbidden() {
        User admin = new User(5L, "root", "r@x", "hash", "ADMIN", null);
        when(userRepository.findById(5L)).thenReturn(Optional.of(admin));
        when(userRepository.countByRole("ADMIN")).thenReturn(1L);

        assertThrows(ValidationException.class, () -> userService.updateUserRole(5L, "CUSTOMER"));
    }

    @Test
    void updateUserRoleDemoteAdminWhenMultipleAllowed() {
        User admin = new User(5L, "root", "r@x", "hash", "ADMIN", null);
        when(userRepository.findById(5L)).thenReturn(Optional.of(admin));
        when(userRepository.countByRole("ADMIN")).thenReturn(2L);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.updateUserRole(5L, "CUSTOMER");

        assertEquals("CUSTOMER", result.getRole());
    }
}
