package in.ecommerce.ecommerce.repo;

import in.ecommerce.ecommerce.entity.Role;
import in.ecommerce.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepo extends JpaRepository<User,Long> {

    // Used during login
    User findByEmail(String email);

    // Check duplicate email during registration
    boolean existsByEmail(String email);

    // Useful for admin panel
    List<User> findByRole(Role role);
}
