package ensah.com.restapi_spring_project.services;

import ensah.com.restapi_spring_project.models.Room;
import ensah.com.restapi_spring_project.repositories.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

   @Autowired
    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }


    public List<Room> getAllRooms() {
       return roomRepository.findAll();
    }
}