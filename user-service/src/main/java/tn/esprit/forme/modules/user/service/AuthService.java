package tn.esprit.forme.modules.user.service;

import tn.esprit.forme.common.ApiException;
import tn.esprit.forme.modules.user.dto.AuthResponse;
import tn.esprit.forme.modules.user.dto.LoginRequest;
import tn.esprit.forme.modules.user.dto.RegisterRequest;
import tn.esprit.forme.modules.partner.service.PartnerClient;

import tn.esprit.forme.modules.user.repository.RoleRepository;
import tn.esprit.forme.modules.user.repository.UserRepository;
import tn.esprit.forme.security.JwtService;
import tn.esprit.forme.modules.user.entity.Role;
import tn.esprit.forme.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PartnerClient partnerClient;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public void register(RegisterRequest req) {
        if (userRepository.existsByEmailIgnoreCase(req.getEmail())) {
            throw new ApiException(409, "Email already exists");
        }

        Long partnerId = null;
        if (req.getPartnerId() != null || req.getPartnerCode() != null) {
            if (req.getPartnerId() == null || req.getPartnerCode() == null) {
                throw new ApiException(400, "partnerId and partnerCode are required together");
            }
            if (!partnerClient.validatePartnerCode(req.getPartnerId(), req.getPartnerCode())) {
                throw new ApiException(400, "Invalid partner code");
            }
            partnerId = req.getPartnerId();
        }

        Role roleUser = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ApiException(500, "ROLE_USER not seeded"));

        User user = User.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail().toLowerCase())
                .password(passwordEncoder.encode(req.getPassword()))
                .profession(req.getProfession())
                .partnerId(partnerId)
                .isActive(true)
                .build();

        user.getRoles().add(roleUser);
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest req) {
        var user = userRepository.findByEmailIgnoreCase(req.getEmail())
                .orElseThrow(() -> new ApiException(401, "Invalid credentials"));

        if (!user.isActive()) throw new ApiException(403, "User is inactive");

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new ApiException(401, "Invalid credentials");
        }

        UserDetails principal = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRoles().stream().map(Role::getName).toArray(String[]::new))
                .build();

        String token = jwtService.generateToken(principal, Map.of(
                "uid", user.getId(),
                "roles", user.getRoles().stream().map(Role::getName).toList()
        ));

        return AuthResponse.builder().token(token).build();
    }
}
