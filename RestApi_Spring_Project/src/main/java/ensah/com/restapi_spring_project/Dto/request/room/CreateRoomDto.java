package ensah.com.restapi_spring_project.Dto.Request.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomDto {

    private String name;
    private int capacity;
}
