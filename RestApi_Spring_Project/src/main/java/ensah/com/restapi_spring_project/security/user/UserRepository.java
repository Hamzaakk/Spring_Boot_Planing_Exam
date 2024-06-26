package ensah.com.restapi_spring_project.security.user;

import ensah.com.restapi_spring_project.models.personnel.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Integer> {
    Optional<User> findByEmail(String email);
    List<User>  findAllByRole(Role role);
}
