package com.littlehorizons.erp.test.fake;

import com.littlehorizons.erp.dao.ParentDao;
import com.littlehorizons.erp.model.Parent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory guardian DAO used to test controllers without a database.
 */
public final class InMemoryParentDao implements ParentDao {

    private final Map<Integer, Parent> parents = new LinkedHashMap<>();
    private int nextId = 1;

    @Override
    public int insert(Parent parent) {
        int id = nextId++;
        parent.setIdParent(id);
        parents.put(id, parent);
        return id;
    }

    @Override
    public void update(Parent parent) {
        parents.put(parent.getIdParent(), parent);
    }

    @Override
    public boolean delete(int idParent) {
        return parents.remove(idParent) != null;
    }

    @Override
    public Parent findById(int idParent) {
        return parents.get(idParent);
    }

    @Override
    public Parent findByDocument(String documentNumber) {
        return parents.values().stream()
                .filter(parent -> documentNumber.equals(parent.getDocumentNumber()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Parent> findAll() {
        return new ArrayList<>(parents.values());
    }

    @Override
    public List<Parent> findByStudent(int idStudent) {
        return new ArrayList<>();
    }
}
