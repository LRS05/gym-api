package com.project.gym.service;

import com.project.gym.config.CustomMetrics;
import com.project.gym.dto.MembershipRequestDTO;
import com.project.gym.dto.MembershipResponseDTO;
import com.project.gym.dto.MembershipStatusRequestDTO;
import com.project.gym.entity.MembershipEntity;
import com.project.gym.entity.UserEntity;
import com.project.gym.entity.enums.MembershipStatus;
import com.project.gym.entity.enums.MembershipType;
import com.project.gym.entity.enums.PaymentMethod;
import com.project.gym.entity.enums.Role;
import com.project.gym.exception.InvalidMembershipAssignmentException;
import com.project.gym.exception.InvalidRoleUpdateException;
import com.project.gym.exception.MembershipNotFoundException;
import com.project.gym.mapper.MembershipMapper;
import com.project.gym.repository.MembershipRepository;
import com.project.gym.repository.UserRepository;
import com.project.gym.data.MembershipTestDataFactory;
import com.project.gym.data.UserTestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MembershipServiceTest
{
    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MembershipMapper membershipMapper;

    @Mock
    private CustomMetrics customMetrics;

    @InjectMocks
    private MembershipService membershipService;

    @Test
    void createMembershipUserRegisteredTest()
    {
        // Given
        UserEntity expectedUser = UserTestDataFactory.userUser();
        String dni = "87654321";
        MembershipRequestDTO requestDTO = new MembershipRequestDTO(
                MembershipType.ANNUALLY,
                PaymentMethod.CARD
        );
        MembershipEntity expectedMembership = MembershipTestDataFactory.userRegisteredMembership();
        MembershipResponseDTO expectedDTO = MembershipTestDataFactory.userRegisteredMembershipDTO();

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(expectedUser));
        when(membershipRepository.save(any(MembershipEntity.class))).thenReturn(expectedMembership);
        when(membershipMapper.toDTO(expectedMembership)).thenReturn(expectedDTO);

        ArgumentCaptor<MembershipEntity> captor = ArgumentCaptor.forClass(MembershipEntity.class);
        MembershipResponseDTO result = membershipService.createMembership(dni, requestDTO);

        // Then
        verify(userRepository).findByDni(dni);
        verify(membershipRepository).save(captor.capture());
        verify(membershipMapper).toDTO(expectedMembership);

        assertEquals(expectedUser, captor.getValue().getUser());
        assertEquals(expectedDTO, result);
        assertAll("membership",
                () -> assertEquals(requestDTO.type(), result.type()),
                () -> assertEquals(requestDTO.paymentMethod(), result.paymentMethod()),
                () -> assertEquals(dni, result.dni())
        );
    }

    @Test
    void createMembershipInvalidUserRoleTest()
    {
        // Given
        String dni = "12345678";
        MembershipRequestDTO requestDTO = new MembershipRequestDTO(
                MembershipType.MONTHLY,
                PaymentMethod.CASH
        );
        UserEntity expectedUser = UserTestDataFactory.userStaff();

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.of(expectedUser));
        assertThrows(InvalidMembershipAssignmentException.class, () -> membershipService.createMembership(dni, requestDTO));

        // Then
        verify(userRepository).findByDni(dni);
        verifyNoInteractions(membershipRepository);

        assertNotEquals(Role.USER, expectedUser.getRole());
    }

    @Test
    void createMembershipUserNotRegistered()
    {
        // Given
        String dni = "99999999";
        MembershipRequestDTO requestDTO = new MembershipRequestDTO(
                MembershipType.MONTHLY,
                PaymentMethod.CASH
        );
        MembershipEntity expectedMembersip = MembershipTestDataFactory.userNotRegisteredMembership();
        MembershipResponseDTO expectedDTO = MembershipTestDataFactory.userNotRegisteredMembershipDTO();

        // When
        when(userRepository.findByDni(dni)).thenReturn(Optional.empty());
        when(membershipRepository.save(any(MembershipEntity.class))).thenReturn(expectedMembersip);
        when(membershipMapper.toDTO(expectedMembersip)).thenReturn(expectedDTO);

        ArgumentCaptor<MembershipEntity> captor = ArgumentCaptor.forClass(MembershipEntity.class);
        MembershipResponseDTO result = membershipService.createMembership(dni, requestDTO);

        // Then
        verify(userRepository).findByDni(dni);
        verify(membershipRepository).save(captor.capture());
        verify(membershipMapper).toDTO(expectedMembersip);

        assertNull(captor.getValue().getUser());
        assertEquals(expectedDTO, result);
        assertAll("membership",
                () -> assertEquals(requestDTO.type(), result.type()),
                () -> assertEquals(requestDTO.paymentMethod(), result.paymentMethod()),
                () -> assertEquals(dni, result.dni()),
                () -> assertNull(expectedMembersip.getUser())
        );
    }

    @Test
    void getMembershipsByDateTest()
    {
        // Given
        List<MembershipEntity> expectedMemberships = MembershipTestDataFactory.membershipListOf2025();
        List<MembershipResponseDTO> expectedDTOs = MembershipTestDataFactory.membershipListOf2025DTO();
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 12, 31);

        // When
        when(membershipRepository.findAllByPaymentDateBetween(start, end)).thenReturn(expectedMemberships);
        when(membershipMapper.toDTO(expectedMemberships)).thenReturn(expectedDTOs);

        List<MembershipResponseDTO> result = membershipService.getMembershipsByDate(start, end);

        // Then
        verify(membershipRepository).findAllByPaymentDateBetween(start, end);
        verify(membershipMapper).toDTO(expectedMemberships);

        assertEquals(expectedDTOs, result);

        assertTrue(result.stream().allMatch(m ->
                !m.paymentDate().isBefore(start) && !m.paymentDate().isAfter(end)
        ));
    }

    @Test
    void getMembershipsByDateEmptyListTest()
    {
        // Given
        LocalDate start = LocalDate.of(2100, 1, 1);
        LocalDate end = LocalDate.of(2100, 12, 31);

        // When
        when(membershipRepository.findAllByPaymentDateBetween(start, end)).thenReturn(new ArrayList<>());
        when(membershipMapper.toDTO(anyList())).thenReturn(new ArrayList<>());

        List<MembershipResponseDTO> result = membershipService.getMembershipsByDate(start, end);

        // Then
        verify(membershipRepository).findAllByPaymentDateBetween(start, end);
        verify(membershipMapper).toDTO(anyList());

        assertTrue(result.isEmpty());
    }

    @Test
    void getMembershipByIdTest()
    {
        // Given
        int id = 1;
        MembershipEntity expectedMembership = MembershipTestDataFactory.userRegisteredMembership();
        MembershipResponseDTO expectedDTO = MembershipTestDataFactory.userRegisteredMembershipDTO();

        // When
        when(membershipRepository.findById(id)).thenReturn(Optional.of(expectedMembership));
        when(membershipMapper.toDTO(expectedMembership)).thenReturn(expectedDTO);

        MembershipResponseDTO result = membershipService.getMembershipById(id);

        // Then
        verify(membershipRepository).findById(id);
        verify(membershipMapper).toDTO(expectedMembership);

        assertEquals(expectedDTO, result);
    }

    @Test
    void getMembershipByIdNotFoundTest()
    {
        // Given
        int id = -1;

        // When
        when(membershipRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(MembershipNotFoundException.class, () -> membershipService.getMembershipById(id));

        // Then
        verify(membershipRepository).findById(id);
    }

    @Test
    void getMembershipsByDniTest()
    {
        // Given
        String dni = "87654321";
        List<MembershipEntity> expectedMemberships = MembershipTestDataFactory.defaultUserMemberships();
        List<MembershipResponseDTO> expectedDTOs = MembershipTestDataFactory.defaultUserMembershipsDTO();

        // When
        when(membershipRepository.findAllByDni(dni)).thenReturn(expectedMemberships);
        when(membershipMapper.toDTO(expectedMemberships)).thenReturn(expectedDTOs);

        List<MembershipResponseDTO> result = membershipService.getMembershipsByDni(dni);

        // Then
        verify(membershipRepository).findAllByDni(dni);
        verify(membershipMapper).toDTO(expectedMemberships);

        assertEquals(expectedDTOs, result);
        assertTrue(result.stream().allMatch(m ->
                m.dni().equals(dni)
        ));
    }

    @Test
    void getMembershipsByDniEmptyListTest()
    {
        // Given
        String dni = "88888888";

        // When
        when(membershipRepository.findAllByDni(dni)).thenReturn(new ArrayList<>());
        when(membershipMapper.toDTO(anyList())).thenReturn(new ArrayList<>());

        List<MembershipResponseDTO> result = membershipService.getMembershipsByDni(dni);

        // Then
        verify(membershipRepository).findAllByDni(dni);
        verify(membershipMapper).toDTO(anyList());

        assertTrue(result.isEmpty());
    }

    @Test
    void getCurrentMembershipByDniTest()
    {
        // Given
        String dni = "87654321";
        MembershipEntity expectedMembership = MembershipTestDataFactory.userRegisteredMembership();
        MembershipResponseDTO expectedDTO = MembershipTestDataFactory.userNotRegisteredMembershipDTO();

        // When
        when(membershipRepository.findFirstByDniOrderByPaymentDateDesc(dni)).thenReturn(Optional.of(expectedMembership));
        when(membershipMapper.toDTO(expectedMembership)).thenReturn(expectedDTO);

        MembershipResponseDTO result = membershipService.getLastMembershipByDni(dni);

        // Then
        verify(membershipRepository).findFirstByDniOrderByPaymentDateDesc(dni);
        verify(membershipMapper).toDTO(expectedMembership);

        assertEquals(expectedDTO, result);
    }

    @Test
    void getCurrentMembershipByDniNotFoundTest()
    {
        // Given
        String dni = "88888888";

        // When
        when(membershipRepository.findFirstByDniOrderByPaymentDateDesc(dni)).thenReturn(Optional.empty());
        assertThrows(MembershipNotFoundException.class, () -> membershipService.getLastMembershipByDni(dni));

        // Then
        verify(membershipRepository).findFirstByDniOrderByPaymentDateDesc(dni);
    }

    @Test
    void getActiveMembershipsTest()
    {
        // Given
        List<MembershipEntity> expectedMemberships = MembershipTestDataFactory.activeMembershipsList();
        List<MembershipResponseDTO> expectedDTOs = MembershipTestDataFactory.activeMembershipsListDTO();

        // When
        when(membershipRepository.findAllByStatus(MembershipStatus.ACTIVE)).thenReturn(expectedMemberships);
        when(membershipMapper.toDTO(expectedMemberships)).thenReturn(expectedDTOs);

        List<MembershipResponseDTO> result = membershipService.getActiveMemberships();

        // Then
        verify(membershipRepository).findAllByStatus(MembershipStatus.ACTIVE);
        verify(membershipMapper).toDTO(expectedMemberships);

        assertEquals(expectedDTOs, result);
        assertTrue(result.stream().allMatch(m ->
                m.status().equals(MembershipStatus.ACTIVE)
        ));
    }

    @Test
    void getActiveMembershipsEmptyListTest()
    {
        // When
        when(membershipRepository.findAllByStatus(MembershipStatus.ACTIVE)).thenReturn(new ArrayList<>());
        when(membershipMapper.toDTO(anyList())).thenReturn(new ArrayList<>());

        List<MembershipResponseDTO> result = membershipService.getActiveMemberships();

        // Then
        verify(membershipRepository).findAllByStatus(MembershipStatus.ACTIVE);
        verify(membershipMapper).toDTO(anyList());

        assertTrue(result.isEmpty());
    }

    @Test
    void deactivateMembershipsTest()
    {
        // Given
        LocalDate actualDate = LocalDate.now();
        List<MembershipEntity> expectedMemberships = List.of(MembershipTestDataFactory.expiredMembership());

        // When
        when(membershipRepository.findAllByStatusAndNextPaymentDateBefore(MembershipStatus.ACTIVE, actualDate)).thenReturn(expectedMemberships);
        when(membershipRepository.saveAll(anyList())).then(invocation -> invocation.getArgument(0));

        ArgumentCaptor<List<MembershipEntity>> captor = ArgumentCaptor.forClass(List.class);

        membershipService.deactivateMemberships();

        // Then
        verify(membershipRepository).findAllByStatusAndNextPaymentDateBefore(MembershipStatus.ACTIVE, actualDate);
        verify(membershipRepository).saveAll(captor.capture());

        assertTrue(captor.getValue().stream().allMatch(m ->
                m.getStatus().equals(MembershipStatus.INACTIVE)
        ));
    }

    @Test
    void deactivateMembershipsEmptyExpiredListTest()
    {
        // Given
        LocalDate actualDate = LocalDate.now();

        // When
        when(membershipRepository.findAllByStatusAndNextPaymentDateBefore(MembershipStatus.ACTIVE, actualDate)).thenReturn(new ArrayList<>());

        membershipService.deactivateMemberships();

        // THen
        verify(membershipRepository).findAllByStatusAndNextPaymentDateBefore(MembershipStatus.ACTIVE, actualDate);
        verifyNoMoreInteractions(membershipRepository);
    }

    @Test
    void updateMembershipStatusByIdTest()
    {
        // Given
        int id = 1;
        MembershipStatusRequestDTO requestDTO = new MembershipStatusRequestDTO(MembershipStatus.INACTIVE);
        MembershipEntity expectedMembership = MembershipTestDataFactory.userRegisteredMembership();
        MembershipEntity expectedNewStatusMembership = MembershipTestDataFactory.userRegisteredMembership();
        expectedNewStatusMembership.setStatus(MembershipStatus.INACTIVE);

        // When
        when(membershipRepository.findById(id)).thenReturn(Optional.of(expectedMembership));
        when(membershipRepository.save(any(MembershipEntity.class))).thenReturn(expectedNewStatusMembership);

        ArgumentCaptor<MembershipEntity> captor = ArgumentCaptor.forClass(MembershipEntity.class);
        MembershipResponseDTO result = membershipService.updateMembershipStatusById(id, requestDTO);

        // Then
        verify(membershipRepository).findById(id);
        verify(membershipRepository).save(captor.capture());

        assertEquals(id, captor.getValue().getId());
        assertEquals(requestDTO.status(), captor.getValue().getStatus());
    }

    @Test
    void updateMembershipsStatusByIdNotFoundTest()
    {
        // Given
        int id = 1;
        MembershipStatusRequestDTO requestDTO = new MembershipStatusRequestDTO(MembershipStatus.ACTIVE);

        // When
        when(membershipRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(MembershipNotFoundException.class, () -> membershipService.updateMembershipStatusById(id, requestDTO));

        // Then
        verify(membershipRepository).findById(id);
        verifyNoMoreInteractions(membershipRepository);
    }

    @Test
    void updateMembershipStatusByIdAlreadyInStatusTest()
    {
        // Given
        int id = 1;
        MembershipStatusRequestDTO requestDTO = new MembershipStatusRequestDTO(MembershipStatus.ACTIVE);
        MembershipEntity expectedMembership = MembershipTestDataFactory.userRegisteredMembership();

        // When
        when(membershipRepository.findById(id)).thenReturn(Optional.of(expectedMembership));

        MembershipResponseDTO result = membershipService.updateMembershipStatusById(id, requestDTO);

        // Then
        verify(membershipRepository).findById(id);
        verifyNoMoreInteractions(membershipRepository);

        assertEquals(requestDTO.status(), expectedMembership.getStatus());
    }

    @Test
    void deleteMembershipByIdTest()
    {
        // Given
        int id = 1;
        MembershipEntity expectedMembership = MembershipTestDataFactory.userRegisteredMembership();

        // When
        when(membershipRepository.findById(id)).thenReturn(Optional.of(expectedMembership));

        ArgumentCaptor<MembershipEntity> captor = ArgumentCaptor.forClass(MembershipEntity.class);
        membershipService.deleteMembershipById(id);

        // Then
        verify(membershipRepository).findById(id);
        verify(membershipRepository).delete(captor.capture());

        assertEquals(expectedMembership, captor.getValue());
    }

    @Test
    void deleteMembershipByIdNotFoundTest()
    {
        // Given
        int id = -1;

        // When
        when(membershipRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(MembershipNotFoundException.class, () -> membershipService.deleteMembershipById(id));

        // Then
        verify(membershipRepository).findById(id);
        verifyNoMoreInteractions(membershipRepository);
    }

    @Test
    void deleteMembershipsByDniTest()
    {
        // Given
        String dni = "87654321";
        List<MembershipEntity> expectedMemberships = List.of(
                MembershipTestDataFactory.userRegisteredMembership(),
                MembershipTestDataFactory.expiredMembership()
        );

        // When
        when(membershipRepository.findAllByDni(dni)).thenReturn(expectedMemberships);

        ArgumentCaptor<List<MembershipEntity>> captor = ArgumentCaptor.forClass(List.class);
        membershipService.deleteMembershipsByDni(dni);

        // Then
        verify(membershipRepository).findAllByDni(dni);
        verify(membershipRepository).deleteAll(captor.capture());

        assertEquals(expectedMemberships, captor.getValue());
        assertTrue(captor.getValue().stream().allMatch(m ->
                m.getDni().equals(dni)
        ));
    }

    @Test
    void deleteMembershipsByDniEmptyListTest()
    {
        // Given
        String dni = "99999999";

        // When
        when(membershipRepository.findAllByDni(dni)).thenReturn(new ArrayList<>());

        membershipService.deleteMembershipsByDni(dni);

        // Then
        verify(membershipRepository).findAllByDni(dni);
        verifyNoMoreInteractions(membershipRepository);
    }
}
