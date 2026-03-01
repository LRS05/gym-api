package com.project.gym.controller;

import com.project.gym.dto.*;
import com.project.gym.service.JwtService;
import com.project.gym.service.MembershipService;
import com.project.gym.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Tag(name = "1 - User")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController
{
    private final UserService userService;
    private final MembershipService membershipService;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<UserResponseDTO> getMe()
    {
        return ResponseEntity.ok(userService.getMe());
    }

    @PatchMapping("/phone-number")
    public ResponseEntity<String> addPhoneNumber(@Valid @RequestBody PhoneNumberRequestDTO requestDTO)
    {
        return ResponseEntity.ok(userService.addPhoneNumber(requestDTO));
    }

    @DeleteMapping
    public ResponseEntity<String> deleteMe(@Valid @RequestBody PasswordRequestDTO requestDTO)
    {
        log.debug("Attempting to delete own account");

        userService.deleteMe(requestDTO);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtService.emptyCookie("access-token").toString())
                .header(HttpHeaders.SET_COOKIE, jwtService.emptyCookie("refresh-token").toString())
                .body("Account deleted successfully.");
    }








    @PostMapping("/memberships")
    public ResponseEntity<MembershipResponseDTO> createMembership(@Valid @RequestBody MembershipRequestDTO requestDTO)
    {
        log.debug("Attempting to create own membership with dni={}, type={}, payment method={}", requestDTO.dni(), requestDTO.type(), requestDTO.paymentMethod());
        return ResponseEntity.status(HttpStatus.CREATED).body(membershipService.create(requestDTO));
    }

    @GetMapping("/memberships")
    public ResponseEntity<List<MembershipResponseDTO>> getMemberships(Principal principal)
    {
        return ResponseEntity.ok(membershipService.getAllByDni(principal.getName()));
    }

    @GetMapping("/memberships/last")
    public ResponseEntity<MembershipResponseDTO> getLastMembership(Principal principal)
    {
        return ResponseEntity.ok(membershipService.getLastByDni(principal.getName()));
    }

}
