package com.project.gym.controller;

import com.project.gym.dto.*;
import com.project.gym.service.MembershipService;
import com.project.gym.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController
{
    private final UserService userService;
    private final MembershipService membershipService;

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
        log.info("Attempting to delete own account");
        userService.deleteMe(requestDTO);
        return ResponseEntity.ok("Account deleted successfully.");
    }



    /*

        Membership Controllers

     */


    @PostMapping("/membership")
    public ResponseEntity<MembershipResponseDTO> createMembership(Principal principal, @Valid @RequestBody MembershipRequestDTO requestDTO)
    {
        log.info("Attempting to create own membership with dni={}, type={}, payment method={}", principal.getName(), requestDTO.type(), requestDTO.paymentMethod());
        return ResponseEntity.status(HttpStatus.CREATED).body(membershipService.createMembershipByDni(principal.getName(), requestDTO));
    }

    @GetMapping("/memberships")
    public ResponseEntity<List<MembershipResponseDTO>> getMemberships(Principal principal)
    {
        return ResponseEntity.ok(membershipService.getAllByDni(principal.getName()));
    }

    @GetMapping("/membership/last")
    public ResponseEntity<MembershipResponseDTO> getLastMembership(Principal principal)
    {
        return ResponseEntity.ok(membershipService.getLastMembershipByDni(principal.getName()));
    }

}
