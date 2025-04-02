package com.example.projectdemo.domain.chat.dao;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class MembershipDAO {

    @Autowired
    private SqlSession mybatis;

    public List<Integer> getChatroomIds(int id) {
        return mybatis.selectList("memberShip.getChatRoomIds", id);
    }

    public int insertMember(int roomId, int empId, int role) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("roomId", roomId);
        param.put("empId", empId);
        param.put("role", role);
        return mybatis.insert("memberShip.insertMember", param);
    }

}
