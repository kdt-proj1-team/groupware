package com.example.projectdemo.domain.employees.mapper;

import com.example.projectdemo.domain.employees.dto.EmployeesDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface EmployeeMapper {

    // 사원번호로 직원 조회
    EmployeesDTO findByEmpNum(@Param("empNum") String empNum);

    // 사원 고유 아이디로 직원 조회
    EmployeesDTO findById(@Param("empId") Integer empId);

    // 이메일로 직원 조회
    EmployeesDTO findByEmail(@Param("email") String email);

    // 사원번호, 이름, 이메일, 주민번호로 직원 조회
    EmployeesDTO findByEmpNumAndNameAndEmailAndSsn(
            @Param("empNum") String empNum,
            @Param("name") String name,
            @Param("email") String email,
            @Param("ssn") String ssn);

    // 회원가입 정보 업데이트
    int updateRegistrationStatus(
            @Param("empNum") String empNum,
            @Param("registered") boolean registered,
            @Param("password") String password,
            @Param("profileImgUrl") String profileImgUrl,
            @Param("phone") String phone,
            @Param("gender") String gender,
            @Param("tempPassword") boolean tempPassword);

    // 비밀번호 업데이트
    int updatePassword(
            @Param("empNum") String empNum,
            @Param("password") String password,
            @Param("tempPassword") boolean tempPassword);

    // 마지막 로그인 시간 업데이트
    int updateLastLogin(
            @Param("empNum") String empNum,
            @Param("lastLogin") LocalDateTime lastLogin);

    // 출결 상태 업데이트 메서드 추가
    int updateAttendStatus(
            @Param("empId") Integer empId,
            @Param("attendStatus") String attendStatus);

}