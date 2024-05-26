package ensah.com.restapi_spring_project.repositories;

import ensah.com.restapi_spring_project.models.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;


@Repository
public interface RoomRepository extends JpaRepository<Room,Integer> {
}