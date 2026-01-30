package com.project.gym.controller;

import com.project.gym.dto.MembershipRequestDTO;
import com.project.gym.dto.MembershipResponseDTO;
import com.project.gym.dto.MembershipStatusRequestDTO;
import com.project.gym.dto.UserResponseDTO;
import com.project.gym.service.MembershipService;
import com.project.gym.service.StaffService;
import com.project.gym.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "2 - Staff")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/staff")
public class StaffController
{
    private final StaffService staffService;
    private final UserService userService;
    private final MembershipService membershipService;

    @GetMapping
    public ResponseEntity<UserResponseDTO> getMe()
    {
        return ResponseEntity.ok(userService.getMe());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getUsers()
    {
        return ResponseEntity.ok(staffService.getUsers());
    }

    @GetMapping("/users/dni/{dni}")
    public ResponseEntity<UserResponseDTO> getUserByDni(@PathVariable String dni)
    {
        log.debug("Attempting to read user with dni={}", dni);
        return ResponseEntity.ok(staffService.getUserByDni(dni));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable int id)
    {
        log.debug("Attempting to read user with id={}", id);
        return ResponseEntity.ok(staffService.getUserById(id));
    }








    @PostMapping("/users/dni/{dni}/memberships")
    public ResponseEntity<MembershipResponseDTO> createMembership(@PathVariable String dni, @Valid @RequestBody MembershipRequestDTO requestDTO)
    {
        log.info("Attempting to create a membership for the user with dni={}, type={}, payment method={}", dni, requestDTO.type(), requestDTO.paymentMethod());
        return ResponseEntity.status(HttpStatus.CREATED).body(membershipService.createMembershipByDni(dni, requestDTO));
    }

    @GetMapping("/users/memberships/{id}")
    public ResponseEntity<MembershipResponseDTO> getMembershipById(@PathVariable int id)
    {
        return ResponseEntity.ok(membershipService.getMembershipById(id));
    }

    @GetMapping("/users/memberships/date")
    public ResponseEntity<List<MembershipResponseDTO>> getMembershipsByDate(@RequestParam LocalDate start, @RequestParam LocalDate end)
    {
        return ResponseEntity.ok(membershipService.getAllByDate(start, end));
    }

    @GetMapping("/users/dni/{dni}/memberships")
    public ResponseEntity<List<MembershipResponseDTO>> getMembershipsByDni(@PathVariable String dni)
    {
        return ResponseEntity.ok(membershipService.getAllByDni(dni));
    }

    @GetMapping("/users/dni/{dni}/memberships/last")
    public ResponseEntity<MembershipResponseDTO> getLastMembershipByDni(@PathVariable String dni)
    {
        return ResponseEntity.ok(membershipService.getLastMembershipByDni(dni));
    }

    @GetMapping("/users/memberships/active")
    public ResponseEntity<List<MembershipResponseDTO>> getActiveMemberships()
    {
        return ResponseEntity.ok(membershipService.getAllActive());
    }

    @PatchMapping("/users/memberships/{id}/status")
    public ResponseEntity<MembershipResponseDTO> updateMembershipStatusById(@PathVariable int id, @Valid @RequestBody MembershipStatusRequestDTO requestDTO)
    {
        log.info("Attempting to update membership with id={} to {}", id, requestDTO.status());
        return ResponseEntity.ok(membershipService.updateMembershipStatusById(id, requestDTO));
    }
}
