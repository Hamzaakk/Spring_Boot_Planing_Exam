package ensah.com.restapi_spring_project.security.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import ensah.com.restapi_spring_project.Dto.Responce.user.UserResponse;
import ensah.com.restapi_spring_project.mappers.UserMapper;
import ensah.com.restapi_spring_project.models.personnel.Admin;
import ensah.com.restapi_spring_project.models.personnel.Prof;
import ensah.com.restapi_spring_project.security.config.JwtService;
import ensah.com.restapi_spring_project.security.token.Token;
import ensah.com.restapi_spring_project.security.token.TokenRepository;
import ensah.com.restapi_spring_project.security.token.TokenType;
import ensah.com.restapi_spring_project.security.user.Role;
import ensah.com.restapi_spring_project.security.user.User;
import ensah.com.restapi_spring_project.security.user.UserRepository;
import ensah.com.restapi_spring_project.services.AdminService;
import ensah.com.restapi_spring_project.services.ProfService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final AdminService adminService;
    private final ProfService profService;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public List<UserResponse> getAllAdmins(){
        List<User> users= userRepository.findAllByRole(Role.ADMIN);
        return users.stream().map(UserMapper::mapToUserResponse).collect(Collectors.toList());
    }
    public List<UserResponse> getAllProfs(){
        List<User> users= userRepository.findAllByRole(Role.PROFESSOR);
        return users.stream().map(UserMapper::mapToUserResponse).collect(Collectors.toList());
    }
    public AuthenticationResponse register(RegisterDto request) {
        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();
        var savedUser =userRepository.save(user);
        // here i will se if User is admin i will persist also in Admin Entity
      if(request.getRole() == Role.ADMIN) {
          Admin admin = new Admin();
          admin.setUser(savedUser);
          adminService.save(admin);
      }
      // here i will se if User is prof i will persist also in Admin Entity
      else if (request.getRole() == Role.PROFESSOR) {
          Prof prof = new Prof();
          prof.setDepartment(null);
          prof.setField(null);
          prof.setUser(savedUser);
          profService.save(prof);
      }

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(savedUser, jwtToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }



    public AuthenticationResponse authenticate(AuthenticationDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokedAllUserTokens(user);
        saveUserToken(user,jwtToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build()
                ;
    }
    private void revokedAllUserTokens(User user){
        var validUserTokens = tokenRepository.findAllByValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(t -> {
            t.setExpired(true);
            t.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }
    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .revoked(false)
                .expired(false)
                .build();
        tokenRepository.save(token);
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if(authHeader == null || !authHeader.startsWith("Bearer")){
            return;
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);
        if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null){
            var user  = this.userRepository.findByEmail(userEmail).orElseThrow();
            if(jwtService.isTokenValid(refreshToken, user)){
                var accessToken = jwtService.generateToken(user);
                revokedAllUserTokens(user);
                saveUserToken(user,accessToken);
                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(),authResponse);
            }

        }
    }
    @Transactional
    public UserResponse update(Integer id, UpdateRequest updateRequest){
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("user not fount"));
        user.setEmail(updateRequest.getEmail());
        user.setFirstName(updateRequest.getFirstName());
        user.setLastName(updateRequest.getLastName());
        user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));

        if(user.getRole() == Role.ADMIN) {
            Admin admin = adminService.getByUser(user);
            admin.setUser(user);
            adminService.save(admin);
        }
        // here i will se if User is prof i will persist also in Admin Entity
        else if (user.getRole() == Role.PROFESSOR) {
            Prof prof = profService.getByUser(user);
            prof.setDepartment(prof.getDepartment());
            prof.setField(prof.getField());
            prof.setGroup(prof.getGroup());
            prof.setUser(user);
            profService.save(prof);
        }
        return UserMapper.mapToUserResponse(user);
    }
}
