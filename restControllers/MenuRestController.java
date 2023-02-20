package com.paracasa.spring.app.restControllers;

import com.paracasa.spring.app.exceptions.ResourceNotFoundException;
import com.paracasa.spring.app.model.Menu;
import com.paracasa.spring.app.security.entity.JwtDto;
import com.paracasa.spring.app.service.menuService.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RequestMapping("/api")
@RestController
public class MenuRestController {
    @Autowired
    private MenuService menuService;

    @Operation(summary = "Retorna todos los menus disponibles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema =
                                    @Schema(implementation =
                                            Menu.class)))
                    })
    })
    @GetMapping("/public/menu")
    ResponseEntity<?> getMenus(){
        List<Menu> menus = menuService.findAll();
        return new ResponseEntity<>(menus, HttpStatus.OK);
    }


    @Operation(summary = "Borra Menú")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",description = "El menú con el " +
                    "id <id> ha sido borrado", content ={@Content}),
            @ApiResponse(responseCode = "403",description = "No tienes permisos para usar esta petición", content ={@Content}),
            @ApiResponse(responseCode = "400",description = "El menú con el " +
                    "id <id> no se ha contrado!", content ={@Content})
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/menu/{id}")
    ResponseEntity<String> delete(@PathVariable("id") Long id) {
        Optional<Menu> menu = menuService.findById(id);
        if(menu.isEmpty()){
            throw new ResourceNotFoundException("El menú con el id "+id+" no se ha contrado!");
        }
        menuService.delete(id);
        return new ResponseEntity<>("El menú con el id "+id+" ha sido borrado", HttpStatus.OK);
    }

    @Operation(summary = "Modifica Menú")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",description = "Producto modificado", content ={@Content}),
            @ApiResponse(responseCode = "403",description = "No tienes permisos para usar esta petición", content ={@Content}),
            @ApiResponse(responseCode = "400",description = "El menú con el " +
                    "id <id> no se ha contrado!", content ={@Content})
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/menu/{id}")
    ResponseEntity<?> update(@PathVariable("id") Long id,
                             @Valid @RequestBody Menu menu) {
        Optional<Menu> menuBd = menuService.findById(id);
        if(menuBd.isEmpty()){
            throw new ResourceNotFoundException("El menú con el id "+id+" no se ha contrado!");
        }
        menu.setName(menu.getName());
        menu.setPrice(menu.getPrice());
        menuService.update(menu);
        return new ResponseEntity<>("Producto modificado", HttpStatus.OK);
    }

    @Operation(summary = "Crear Menú")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", content ={@Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation =
                            Menu.class))}),
            @ApiResponse(responseCode = "403",description = "No tienes permisos para usar esta petición", content ={@Content}),
            @ApiResponse(responseCode = "400",description = "El precio es " +
                    "obligatorio / El nombre es obligatorio", content ={@Content})
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/menu/create")
    ResponseEntity<?> create(@RequestBody Menu menu) {
        System.out.println("menu body name: " + menu.getName());
        System.out.println("menu body price: " + menu.getPrice());
        if(menu.getPrice() == 0.0){
            return new ResponseEntity<>("El precio es obligatorio",
                    HttpStatus.BAD_REQUEST);
        }
        if(menu.getName() == null){
            return new ResponseEntity<>("El nombre es obligatorio",
                    HttpStatus.BAD_REQUEST);
        }
        menuService.create(menu);
        return new ResponseEntity<>(menu, HttpStatus.CREATED);
    }
}