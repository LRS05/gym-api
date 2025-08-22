package com.project.gym.controller;

import com.project.gym.dto.MembershipRequestDTO;
import com.project.gym.dto.MembershipResponseDTO;
import com.project.gym.dto.MembershipStatusRequestDTO;
import com.project.gym.dto.UserResponseDTO;
import com.project.gym.service.MembershipService;
import com.project.gym.service.StaffService;
import com.project.gym.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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
        return ResponseEntity.ok(userService.getUser());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getUsers()
    {
        return ResponseEntity.ok(staffService.getUsers());
    }

    @GetMapping("/user/dni/{dni}")
    public ResponseEntity<UserResponseDTO> getUserByDni(@PathVariable String dni)
    {
        log.info("Attempting to read user with dni={}", dni);
        return ResponseEntity.ok(staffService.getUserByDni(dni));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable int id)
    {
        log.info("Attempting to read user with id={}", id);
        return ResponseEntity.ok(staffService.getUserById(id));
    }



    /*

           Membership Controllers

     */



    @PostMapping("/membership/dni/{dni}")
    public ResponseEntity<MembershipResponseDTO> createMembership(@PathVariable String dni, @Valid @RequestBody MembershipRequestDTO requestDTO)
    {
        log.info("Attempting to create a membership for the user with dni={}, type={}, payment method={}", dni, requestDTO.type(), requestDTO.paymentMethod());
        return ResponseEntity.ok(membershipService.createMembership(dni, requestDTO));
    }

    @GetMapping("/memberships/date")
    public ResponseEntity<List<MembershipResponseDTO>> getMembershipsByDate(@RequestParam LocalDate start, @RequestParam LocalDate end)
    {
        return ResponseEntity.ok(membershipService.getMembershipsByDate(start, end));
    }

    @GetMapping("/memberships/dni/{dni}")
    public ResponseEntity<List<MembershipResponseDTO>> getMembershipsByDni(@PathVariable String dni)
    {
        return ResponseEntity.ok(membershipService.getMembershipsByDni(dni));
    }

    @GetMapping("/membership/dni/{dni}/last")
    public ResponseEntity<MembershipResponseDTO> getLastMembershipByDni(@PathVariable String dni)
    {
        return ResponseEntity.ok(membershipService.getLastMembershipByDni(dni));
    }

    @GetMapping("/memberships/active")
    public ResponseEntity<List<MembershipResponseDTO>> getActiveMemberships()
    {
        return ResponseEntity.ok(membershipService.getActiveMemberships());
    }

    @PatchMapping("/membership/{id}/status")
    public ResponseEntity<MembershipResponseDTO> updateMembershipStatusById(@PathVariable int id, @Valid @RequestBody MembershipStatusRequestDTO requestDTO)
    {
        log.info("Attempting to update membership with id={} to {}", id, requestDTO.status());
        return ResponseEntity.ok(membershipService.updateMembershipStatusById(id, requestDTO));
    }
}
