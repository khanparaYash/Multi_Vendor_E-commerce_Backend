package in.ecommerce.ecommerce.security;

import in.ecommerce.ecommerce.entity.Role;
import in.ecommerce.ecommerce.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserPrincipalTest {

    @Test
    void testGetAuthorities_Admin() {
        User user = new User();
        user.setRole(Role.ADMIN);
        UserPrincipal principal = new UserPrincipal(user);

        Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testGetAuthorities_Vendor() {
        User user = new User();
        user.setRole(Role.VENDOR);
        UserPrincipal principal = new UserPrincipal(user);

        Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_VENDOR")));
    }

    @Test
    void testGetAuthorities_Customer() {
        User user = new User();
        user.setRole(Role.CUSTOMER);
        UserPrincipal principal = new UserPrincipal(user);

        Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER")));
    }
}
