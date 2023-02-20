package com.paracasa.spring.app.restControllers;

import com.paracasa.spring.app.model.Role;
import com.paracasa.spring.app.model.Usuario;
import com.paracasa.spring.app.security.entity.JwtDto;
import com.paracasa.spring.app.security.entity.LoginUsuario;
import com.paracasa.spring.app.security.entity.NuevoUsuario;
import com.paracasa.spring.app.security.enums.RoleName;
import com.paracasa.spring.app.security.jwt.JwtProvider;
import com.paracasa.spring.app.service.roleService.RoleService;
import com.paracasa.spring.app.service.usuarioService.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    RoleService roleService;

    @Autowired
    JwtProvider jwtProvider;

    @Operation(summary = "Crear usuario nuevo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario creado",
                   content = @Content),
            @ApiResponse(responseCode = "400", description = "Usuario mal " +
                    "creado / Ese nombre de usuario ya existe / Ese email ya " +
                    "existe",
                    content = @Content)
                    })
    @PostMapping("/new")
    public ResponseEntity<?> create(@Valid @RequestBody NuevoUsuario nuevoUsuario, BindingResult bindingResult){
        if(bindingResult.hasErrors())
            return new ResponseEntity("Usuario mal creado",
                    HttpStatus.BAD_REQUEST);
        if(usuarioService.existByUserName(nuevoUsuario.getUsername()))
            return new ResponseEntity<>("Ese nombre de usuario ya existe",
                    HttpStatus.BAD_REQUEST);
        if(usuarioService.existByEmail(nuevoUsuario.getEmail()))
            return new ResponseEntity<>("Ese email ya existe",
                    HttpStatus.BAD_REQUEST);
        Usuario usuario = new Usuario(nuevoUsuario.getName(),
                nuevoUsuario.getEmail(), nuevoUsuario.getUsername(),
                passwordEncoder.encode(nuevoUsuario.getPassword()));
        Set<Role> roles = new HashSet<>();
        roles.add(roleService.findByRoleName(RoleName.ROLE_USER).get());
        if(nuevoUsuario.getRoles().contains("admin"))
            roles.add(roleService.findByRoleName(RoleName.ROLE_ADMIN).get());
        usuario.setRoles(roles);
        usuarioService.registrar(usuario);
        return new ResponseEntity<>("Usuario creado", HttpStatus.CREATED);
    }

    @Operation(summary = "Login usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario creado",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation =
                                            JwtDto.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Campos erróneos",
                    content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<JwtDto> login(@Valid @RequestBody LoginUsuario loginUsuario, BindingResult bindingResult){
        if(bindingResult.hasErrors())
            return new ResponseEntity("Campos erróneos",
                    HttpStatus.BAD_REQUEST);
        Authentication authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginUsuario.getUsername(), loginUsuario.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtProvider.generateToken(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        JwtDto jwtDto = new JwtDto(jwt, userDetails.getUsername(),
                userDetails.getAuthorities());
        return new ResponseEntity<>(jwtDto, HttpStatus.OK);
    }
}
