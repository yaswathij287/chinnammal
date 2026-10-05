package com.smarthostel.controller;

import com.smarthostel.exception.DatabaseException;
import com.smarthostel.exception.RoomFullException;
import com.smarthostel.exception.StudentNotFoundException;
import com.smarthostel.exception.ValidationException;
import com.smarthostel.model.Student;
import com.smarthostel.service.StudentService;

import java.util.List;

/**
 * Controller mediating student operations.
 */
public class StudentController {

    private final StudentService studentService;

    public StudentController() {
        this.studentService = new StudentService();
    }

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    public List<Student> getAllStudents() throws DatabaseException {
        return studentService.getAllStudents();
    }

    public List<Student> getStudentsByBlock(int blockId) throws DatabaseException {
        return studentService.getStudentsByBlock(blockId);
    }

    public List<Student> searchStudents(String query) throws DatabaseException {
        return studentService.searchStudents(query);
    }

    public com.smarthostel.model.StudentRegistrationResult registerStudent(Student student) throws ValidationException, RoomFullException, DatabaseException {
        return studentService.registerStudentWithAccount(student);
    }

    public Student addStudent(Student student) throws ValidationException, RoomFullException, DatabaseException {
        return studentService.addStudent(student);
    }

    public void updateStudent(Student student) throws ValidationException, RoomFullException, StudentNotFoundException, DatabaseException {
        studentService.updateStudent(student);
    }

    public void deleteStudent(int studentId) throws StudentNotFoundException, DatabaseException {
        studentService.deleteStudent(studentId);
    }

    public void vacateStudent(int studentId) throws StudentNotFoundException, DatabaseException {
        studentService.vacateStudent(studentId);
    }

    public List<Student> getRoommates(int studentId) throws DatabaseException {
        return studentService.getRoommates(studentId);
    }
}
