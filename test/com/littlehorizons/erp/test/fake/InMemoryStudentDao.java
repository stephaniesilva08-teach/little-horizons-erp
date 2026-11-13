package com.littlehorizons.erp.test.fake;

import com.littlehorizons.erp.dao.StudentDao;
import com.littlehorizons.erp.model.Student;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory student DAO used to test controllers without a database.
 */
public final class InMemoryStudentDao implements StudentDao {

    private final Map<Integer, Student> students = new LinkedHashMap<>();
    private final Map<String, Integer> documents = new LinkedHashMap<>();
    private final List<String> guardians = new ArrayList<>();
    private int nextId = 1;

    @Override
    public int insert(Student student) {
        int id = nextId++;
        student.setIdStudent(id);
        students.put(id, student);
        documents.put(student.getDocumentNumber(), id);
        return id;
    }

    @Override
    public int insertWithGuardian(Student student, int idParent) {
        int id = insert(student);
        assignGuardian(id, idParent, true, true);
        return id;
    }

    @Override
    public void update(Student student) {
        students.put(student.getIdStudent(), student);
        documents.put(student.getDocumentNumber(), student.getIdStudent());
    }

    @Override
    public boolean delete(int idStudent) {
        Student removed = students.remove(idStudent);
        if (removed != null) {
            documents.remove(removed.getDocumentNumber());
        }
        return removed != null;
    }

    @Override
    public Student findById(int idStudent) {
        return students.get(idStudent);
    }

    @Override
    public Student findByDocument(String documentNumber) {
        Integer id = documents.get(documentNumber);
        return id == null ? null : students.get(id);
    }

    @Override
    public List<Student> findAll() {
        return new ArrayList<>(students.values());
    }

    @Override
    public List<Student> findActive() {
        return findAll();
    }

    @Override
    public List<Student> search(String text) {
        return findAll();
    }

    @Override
    public void assignGuardian(int idStudent, int idParent, boolean main, boolean emergency) {
        guardians.add(idStudent + ":" + idParent + ":" + main + ":" + emergency);
    }

    @Override
    public boolean exists(int idStudent) {
        return students.containsKey(idStudent);
    }

    public List<String> guardians() {
        return guardians;
    }

    public int count() {
        return students.size();
    }
}
