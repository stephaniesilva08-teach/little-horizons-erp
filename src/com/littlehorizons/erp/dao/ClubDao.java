package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.model.Membership;
import java.util.List;

/**
 * Persistence contract for clubs.
 */
public interface ClubDao {

    int insert(Club club);

    void update(Club club);

    boolean delete(int idClub);

    Club findById(int idClub);

    List<Club> findAll();

    List<Club> findActive();

    int insertMembership(Membership membership);


    void updateMembership(Membership membership);

    boolean deleteMembership(int idMembership);

    Membership findMembershipById(int idMembership);

    List<Membership> findMembershipsByClub(int idClub);

    List<Membership> findMembershipsByStudent(int idStudent);

    List<Membership> findAllMemberships();

    int countActiveMembers(int idClub);
}
