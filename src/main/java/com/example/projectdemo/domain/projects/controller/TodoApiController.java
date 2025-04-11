package com.example.projectdemo.domain.projects.controller;

import com.example.projectdemo.domain.projects.dto.TodoDTO;
import com.example.projectdemo.domain.projects.service.TodoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/todos")
public class TodoApiController {

    @Autowired
    private TodoService todoService;

    /**
     * 직원의 할 일 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<TodoDTO>> getTodoListByEmployee(HttpServletRequest request) {
        String empNum = (String) request.getAttribute("empNum");
        if (empNum == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(todoService.getTodoListByEmployee(empNum));
    }

    /**
     * 특정 날짜의 할 일 목록 조회
     */
    @GetMapping("/date")
    public ResponseEntity<List<TodoDTO>> getTodosByDate(
            HttpServletRequest request,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        String empNum = (String) request.getAttribute("empNum");
        if (empNum == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(todoService.getTodosByDate(empNum, date));
    }

    /**
     * 우선순위별 할 일 목록 조회
     */
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<TodoDTO>> getTodosByPriority(
            HttpServletRequest request,
            @PathVariable String priority) {
        String empNum = (String) request.getAttribute("empNum");
        if (empNum == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(todoService.getTodosByPriority(empNum, priority));
    }

    /**
     * 할 일 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<TodoDTO> getTodoById(@PathVariable Integer id) {
        TodoDTO todo = todoService.getTodoById(id);
        if (todo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(todo);
    }

    /**
     * 새 할 일 추가
     */
    @PostMapping
    public ResponseEntity<TodoDTO> createTodo(
            @RequestBody TodoDTO todo,
            HttpServletRequest request) {
        String empNum = (String) request.getAttribute("empNum");
        if (empNum == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        todo.setEmpNum(empNum);
        TodoDTO createdTodo = todoService.createTodo(todo);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTodo);
    }

    /**
     * 할 일 업데이트
     */
    @PutMapping("/{id}")
    public ResponseEntity<TodoDTO> updateTodo(
            @PathVariable Integer id,
            @RequestBody TodoDTO todo,
            HttpServletRequest request) {
        String empNum = (String) request.getAttribute("empNum");
        if (empNum == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 본인의 할 일인지 확인
        TodoDTO existingTodo = todoService.getTodoById(id);
        if (existingTodo == null || !existingTodo.getEmpNum().equals(empNum)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        todo.setId(id);
        todo.setEmpNum(empNum);
        TodoDTO updatedTodo = todoService.updateTodo(todo);
        return ResponseEntity.ok(updatedTodo);
    }

    /**
     * 할 일 완료 상태 토글
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<TodoDTO> toggleTodoCompletion(
            @PathVariable Integer id,
            HttpServletRequest request) {
        String empNum = (String) request.getAttribute("empNum");
        if (empNum == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 본인의 할 일인지 확인
        TodoDTO existingTodo = todoService.getTodoById(id);
        if (existingTodo == null || !existingTodo.getEmpNum().equals(empNum)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        TodoDTO updatedTodo = todoService.toggleTodoCompletion(id);
        return ResponseEntity.ok(updatedTodo);
    }

    /**
     * 할 일 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodo(
            @PathVariable Integer id,
            HttpServletRequest request) {
        String empNum = (String) request.getAttribute("empNum");
        if (empNum == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 본인의 할 일인지 확인
        TodoDTO existingTodo = todoService.getTodoById(id);
        if (existingTodo == null || !existingTodo.getEmpNum().equals(empNum)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        todoService.deleteTodo(id);
        return ResponseEntity.ok().build();
    }
}