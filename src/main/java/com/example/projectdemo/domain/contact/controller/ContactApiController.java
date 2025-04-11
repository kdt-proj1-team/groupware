package com.example.projectdemo.domain.contact.controller;

import com.example.projectdemo.domain.contact.dto.EmployeeContactDTO;
import com.example.projectdemo.domain.contact.dto.PersonalContactDTO;
import com.example.projectdemo.domain.contact.service.ContactService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contact")
public class ContactApiController {
    private final ContactService contactService;

    @Autowired
    public ContactApiController(ContactService contactService) {
        this.contactService = contactService;
    }

    /**
     * 공유 주소록(사원 연락처) 조회
     */
    @GetMapping("/shared")
    public ResponseEntity<List<EmployeeContactDTO>> getSharedContacts(@RequestParam(required = false) String dept) {
        try {
            List<EmployeeContactDTO> sharedContacts;

            // 'all'도 null처럼 간주하여 전체 조회
            if (dept != null && !dept.isEmpty() && !dept.equals("all")) {
                sharedContacts = contactService.getSharedContactsByDepartment(dept);
            } else {
                sharedContacts = contactService.getSharedContacts();
            }

            if (sharedContacts.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }

            return ResponseEntity.ok(sharedContacts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    /**
     * 개인 주소록(사원 연락처) 조회
     */
    @GetMapping("/personal")
    public ResponseEntity<List<PersonalContactDTO>> getPersonalContacts(HttpServletRequest request) {
        try {
            Integer empId = (Integer) request.getAttribute("id");
            if (empId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            List<PersonalContactDTO> personalContacts = contactService.getPersonalContactsByEmpId(empId);

            return ResponseEntity.ok(personalContacts);
        } catch (Exception e) {
            // 예외 발생 시 INTERNAL_SERVER_ERROR(500) 응답
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 개인 주소록에 연락처 추가
     */
    @PostMapping("/personal/add")
    public ResponseEntity<PersonalContactDTO> addPersonalContact(@RequestBody PersonalContactDTO contact,
                                                                 HttpServletRequest request) {
        try {
            Integer empId = (Integer) request.getAttribute("id");
            if (empId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            contactService.addPersonalContact(empId, contact);
            return ResponseEntity.status(HttpStatus.CREATED).build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    /**
     * 개인 주소록 연락처 삭제
     */
    @DeleteMapping("/personal/delete")
    public ResponseEntity<Void> deletePersonalContacts(@RequestBody List<Integer> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.badRequest().build(); // 잘못된 요청
            }

            contactService.deletePersonalContacts(ids); // 서비스 호출
            return ResponseEntity.ok().build(); // 성공
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 서버 오류
        }
    }


    /**
     * 개인 주소록 연락처 수정
     */
    @PutMapping("/personal/{id}")
    public ResponseEntity<Void> updatePersonalContact(
            @PathVariable int id,
            @RequestBody PersonalContactDTO dto
    ) {
        try {
            dto.setId(id); // id 바인딩
            contactService.updatePersonalContact(dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



}
