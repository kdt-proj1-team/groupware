package com.example.projectdemo.domain.booking.service;

import com.example.projectdemo.domain.booking.dto.BookingRequestDTO;
import com.example.projectdemo.domain.booking.dto.SuppliesDTO;
import com.example.projectdemo.domain.booking.dto.SuppliesBookingDTO;
import com.example.projectdemo.domain.booking.entity.Supplies;
import com.example.projectdemo.domain.booking.entity.SuppliesBooking;
import com.example.projectdemo.domain.booking.mapper.SuppliesMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SuppliesService {

    @Autowired
    private SuppliesMapper suppliesMapper;

    // 모든 비품 조회
    public List<SuppliesDTO> getAllSupplies() {
        return suppliesMapper.findAllSupplies().stream()
                .map(this::convertToDtoWithRealTimeAvailability)
                .collect(Collectors.toList());
    }

    // 특정 비품 조회
    public SuppliesDTO getSuppliesById(Integer id) {
        Supplies supply = suppliesMapper.findSuppliesById(id);
        return convertToDtoWithRealTimeAvailability(supply);
    }

    // 모든 비품 예약 조회
    public List<SuppliesBookingDTO> getAllBookings() {
        return suppliesMapper.findAllSuppliesBookings().stream()
                .map(this::convertToBookingDto)
                .collect(Collectors.toList());
    }

    // 특정 사원의 비품 예약 조회
    public List<SuppliesBookingDTO> getBookingsByEmpNum(String empNum) {
        LocalDateTime now = LocalDateTime.now();
        return suppliesMapper.findSuppliesBookingsByEmpNum(empNum).stream()
                .map(this::convertToBookingDto).filter(dto -> dto.getStart() != null && dto.getStart().isAfter(now))
                .collect(Collectors.toList());
    }

    // 특정 비품의 예약 조회
    public List<SuppliesBookingDTO> getBookingsBySupplyId(Integer supplyId) {
        return suppliesMapper.findSuppliesBookingsBySupplyId(supplyId).stream()
                .map(this::convertToBookingDto)
                .collect(Collectors.toList());
    }

    // 특정 날짜 범위의 비품 예약 조회
    public List<SuppliesBookingDTO> getBookingsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return suppliesMapper.findSuppliesBookingsByDateRange(startDate, endDate).stream()
                .map(this::convertToBookingDto)
                .collect(Collectors.toList());
    }

    // 비품 예약 가능 여부 확인
    public boolean isSupplyAvailable(Integer supplyId, Integer quantity, LocalDateTime startTime, LocalDateTime endTime) {
        return suppliesMapper.isSupplyAvailable(supplyId, quantity, startTime, endTime, null);
    }

    // 특정 시간대에 사용 가능한 비품 수량 조회
    public int getAvailableQuantityInPeriod(Integer supplyId, LocalDateTime startTime, LocalDateTime endTime) {
        return suppliesMapper.getAvailableQuantityInPeriod(supplyId, startTime, endTime);
    }

    // 엔티티를 DTO로 변환하되 실시간 가용 수량 계산
    private SuppliesDTO convertToDtoWithRealTimeAvailability(Supplies supplies) {
        if (supplies == null) return null;

        // 현재 시점의 예약된 수량 계산
        LocalDateTime now = LocalDateTime.now();
        int bookedQuantity = suppliesMapper.getBookedQuantityAtTime(supplies.getId(), now);

        // 실시간 가용 수량 계산
        int realTimeAvailableQuantity = supplies.getTotalQuantity() - bookedQuantity;

        return new SuppliesDTO(
                supplies.getId(),
                supplies.getName(),
                supplies.getCategory(),
                supplies.getTotalQuantity(),
                realTimeAvailableQuantity,
                supplies.getDescription(),
                supplies.getIsAvailable()
        );
    }

    @Transactional
    public List<SuppliesBookingDTO> createMultipleBookings(String empNum, BookingRequestDTO requestDTO) {
        List<SuppliesBookingDTO> bookings = new ArrayList<>();

        for (BookingRequestDTO.SupplyItem item : requestDTO.getSupplies()) {
            LocalDateTime startDateTime = parseDateTime(requestDTO.getStartDate(), requestDTO.getStartTime());
            LocalDateTime endDateTime = parseDateTime(requestDTO.getEndDate(), requestDTO.getEndTime());

            // 비품 예약 가능 여부 확인
            if (!isSupplyAvailable(item.getId(), item.getQuantity(), startDateTime, endDateTime)) {
                throw new RuntimeException("비품 " + item.getId() + " 예약 불가");
            }

            // 예약 생성
            SuppliesBooking booking = new SuppliesBooking();
            booking.setSupplyId(item.getId());
            booking.setEmpNum(empNum);
            booking.setQuantity(item.getQuantity());
            booking.setStart(startDateTime);
            booking.setEnd(endDateTime);
            booking.setPurpose(requestDTO.getPurpose());
            booking.setBookingStatus("CONFIRMED");
            booking.setCreatedAt(LocalDateTime.now());

            suppliesMapper.insertSuppliesBooking(booking);
            bookings.add(convertToBookingDto(booking));
        }

        return bookings;
    }

    // 비품 예약 수정
    @Transactional
    public SuppliesBookingDTO updateBooking(Integer id, BookingRequestDTO requestDTO) {
        // 기존 예약 조회
        SuppliesBooking existingBooking = suppliesMapper.findAllSuppliesBookings().stream()
                .filter(b -> b.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("예약 정보를 찾을 수 없습니다."));

        // 문자열 날짜/시간을 LocalDateTime으로 변환
        LocalDateTime startDateTime = parseDateTime(requestDTO.getStartDate(), requestDTO.getStartTime());
        LocalDateTime endDateTime = parseDateTime(requestDTO.getEndDate(), requestDTO.getEndTime());

        // 비품 ID, 수량 또는 시간이 변경된 경우, 예약 가능 여부 확인
        if (!existingBooking.getSupplyId().equals(requestDTO.getSupplyId()) ||
                !existingBooking.getQuantity().equals(requestDTO.getQuantity()) ||
                !existingBooking.getStart().equals(startDateTime) ||
                !existingBooking.getEnd().equals(endDateTime)) {

            if (!suppliesMapper.isSupplyAvailable(requestDTO.getSupplyId(), requestDTO.getQuantity(),
                    startDateTime, endDateTime, id)) {
                throw new RuntimeException("해당 시간에 비품을 예약할 수 없습니다.");
            }
        }

        // 예약 정보 업데이트
        existingBooking.setSupplyId(requestDTO.getSupplyId());
        existingBooking.setQuantity(requestDTO.getQuantity());
        existingBooking.setStart(startDateTime);
        existingBooking.setEnd(endDateTime);
        existingBooking.setPurpose(requestDTO.getPurpose());

        // 예약 수정
        suppliesMapper.updateSuppliesBooking(existingBooking);

        // 수정된 예약 반환
        return convertToBookingDto(existingBooking);
    }

    // 비품 예약 취소
    @Transactional
    public boolean cancelBooking(Integer id) {
        // 기존 예약 조회
        SuppliesBooking existingBooking = suppliesMapper.findAllSuppliesBookings().stream()
                .filter(b -> b.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("예약 정보를 찾을 수 없습니다."));

        // 예약 취소
        return suppliesMapper.cancelSuppliesBooking(id) > 0;
    }

    // 날짜 및 시간 문자열을 LocalDateTime으로 변환하는 유틸리티 메서드
    private LocalDateTime parseDateTime(String dateString, String timeString) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        LocalDate date = LocalDate.parse(dateString, dateFormatter);
        LocalTime time = LocalTime.parse(timeString, timeFormatter);

        return LocalDateTime.of(date, time);
    }


    // 예약 엔티티를 DTO로 변환하는 유틸리티 메서드
    private SuppliesBookingDTO convertToBookingDto(SuppliesBooking booking) {
        if (booking == null) return null;

        SuppliesBookingDTO dto = new SuppliesBookingDTO();
        dto.setId(booking.getId());
        dto.setSupplyId(booking.getSupplyId());
        dto.setEmpNum(booking.getEmpNum());
        dto.setQuantity(booking.getQuantity());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setPurpose(booking.getPurpose());
        dto.setBookingStatus(booking.getBookingStatus());
        dto.setCreatedAt(booking.getCreatedAt());

        // 비품 정보가 있는 경우 설정
        if (booking.getSupplies() != null) {
            dto.setSupplyName(booking.getSupplies().getName());
            dto.setCategory(booking.getSupplies().getCategory());
        }

        // 직원 정보가 있는 경우 설정
        dto.setEmpName(booking.getEmpName());
        dto.setDeptName(booking.getDeptName());

        return dto;
    }
    /**
     * 특정 시간 범위 내에 시작하는 비품 예약 목록 조회
     * 알림을 위해 사용됨
     *
     * @param startTimeFrom 시작 시간 범위 (이 시간 이후)
     * @param startTimeTo 시작 시간 범위 (이 시간 이전)
     * @return 해당 시간 범위 내에 시작하는 비품 예약 목록
     */
    public List<SuppliesBooking> getBookingsStartingBetween(LocalDateTime startTimeFrom, LocalDateTime startTimeTo) {
        return suppliesMapper.findBookingsStartingBetween(startTimeFrom, startTimeTo);
    }

}