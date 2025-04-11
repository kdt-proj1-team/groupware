package com.example.projectdemo.domain.contact.service;

import com.example.projectdemo.domain.contact.dto.EmployeeContactDTO;
import com.example.projectdemo.domain.contact.dto.PersonalContactDTO;
import com.example.projectdemo.domain.contact.mapper.ContactMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactService {
    @Autowired
    private ContactMapper contactMapper;

    /**
     * 공유주소록(사원연락처) 조회
     */
    public List<EmployeeContactDTO> getSharedContacts() {
        return contactMapper.findAllEmpContacts();
    }

    /**
     * 부서별 공유주소록(사원연락처) 조회
     */
    public List<EmployeeContactDTO> getSharedContactsByDepartment(String depName){
        return contactMapper.findEmpContactsByDepartment(depName);
    }

    /**
     * 개인 주소록(사원 연락처) 조회
     */
    public List<PersonalContactDTO> getPersonalContactsByEmpId(Integer empId) {
        return contactMapper.findPersonalContactsByEmpId(empId);
    }

    /**
     * 개인 주소록에 연락처 추가
     */
    public void addPersonalContact(Integer empId, PersonalContactDTO contact) {
        contact.setEmpId(empId);
        contactMapper.insertPersonalContact(contact);
    }

    /**
     * 개인 주소록 연락처 삭제
     */
    public void deletePersonalContacts(List<Integer> ids) {
        contactMapper.deleteContactsByIds(ids);
    }

    /**
     * 개인 주소록 연락처 수정
     */
    public void updatePersonalContact(PersonalContactDTO dto) {
        contactMapper.updatePersonalContact(dto);
    }


}
