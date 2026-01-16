package com.project.gym.controller;

import com.project.gym.dto.*;
import com.project.gym.service.AdminService;
import com.project.gym.service.MembershipService;
import com.project.gym.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController
{
    private final AdminService adminService;
    private final MembershipService membershipService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserResponseDTO> getMe()
    {
        return ResponseEntity.ok(userService.getMe());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getUsers()
    {
        return ResponseEntity.ok(adminService.getUsers());
    }

    @GetMapping("/users/dni/{dni}")
    public ResponseEntity<UserResponseDTO> getUserByDni(@PathVariable String dni)
    {
        return ResponseEntity.ok(adminService.getUserByDni(dni));
    }

    @PatchMapping(value = "/users/dni/{dni}/role")
    public ResponseEntity<UserResponseDTO> updateUserRoleByDni(@PathVariable String dni, @Valid @RequestBody RoleRequestDTO requestDTO)
    {
        log.debug("Attempting to update user with dni={} to {}", dni, requestDTO.role());
        return ResponseEntity.ok(adminService.updateUserRoleByDni(dni, requestDTO));
    }

    @DeleteMapping("/users/dni/{dni}")
    public ResponseEntity<String> deleteUserByDni(@PathVariable String dni)
    {
        log.debug("Attempting to delete user with dni={}", dni);
        adminService.deleteUserByDni(dni);
        return ResponseEntity.ok("User with dni " + dni + " successfully deleted.");
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUserById(@PathVariable int id)
    {
        log.debug("Attempting to delete user with id={}", id);
        adminService.deleteUserById(id);
        return ResponseEntity.ok("User with id " + id + " successfully deleted.");
    }









    @PostMapping("/users/dni/{dni}/memberships")
    public ResponseEntity<MembershipResponseDTO> createMembershipByDni(@PathVariable String dni, @Valid @RequestBody MembershipRequestDTO requestDTO)
    {
        log.debug("Attempting to create a membership for the user with dni={}, type={}, payment method={}", dni, requestDTO.type(), requestDTO.paymentMethod());
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
        log.debug("Attempting to update membership with id={} to {}", id, requestDTO.status());
        return ResponseEntity.ok(membershipService.updateMembershipStatusById(id, requestDTO));
    }

    @DeleteMapping("/users/dni/{dni}/memberships")
    public ResponseEntity<String> deleteMembershipsByDni(@PathVariable String dni)
    {
        log.debug("Attempting to delete all the memberships of the user with dni={}", dni);
        membershipService.deleteAllByDni(dni);
        return ResponseEntity.ok("Memberships of the user with dni " + dni + " successfully deleted.");
    }

    @DeleteMapping("/users/memberships/{id}")
    public ResponseEntity<String> deleteMembershipById(@PathVariable int id)
    {
        log.debug("Attempting to delete membership with id={}", id);
        membershipService.deleteMembershipById(id);
        return ResponseEntity.ok("Membership with id " + id + " successfully deleted.");
    }
}
