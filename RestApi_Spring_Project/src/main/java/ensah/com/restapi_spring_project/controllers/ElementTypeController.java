package ensah.com.restapi_spring_project.controllers;


import ensah.com.restapi_spring_project.Dto.Responce.FiledDto;
import ensah.com.restapi_spring_project.models.ElementType;
import ensah.com.restapi_spring_project.services.ElementTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController

@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class ElementTypeController {

    private final ElementTypeService elementTypeService;

    @Autowired
    public ElementTypeController(ElementTypeService elementTypeService) {
        this.elementTypeService = elementTypeService;
    }


    @GetMapping("/element_type")
    @PreAuthorize("hasAuthority('admin:read')")
    public List<ElementType> getAllElementType() {

        return elementTypeService.getAllElementType();
    }
}