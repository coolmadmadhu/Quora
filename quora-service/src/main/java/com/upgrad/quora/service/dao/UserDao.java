package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.UserAuth;
import com.upgrad.quora.service.entity.UserEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class UserDao {

    @PersistenceContext
    private EntityManager entityManager;

    public UserEntity createUser(UserEntity userEntity) {
        entityManager.persist(userEntity);
        return userEntity;
    }

    public UserEntity getUserByEmail(final String email) {
        try {
            return entityManager.createNamedQuery("userByEmail", UserEntity.class).setParameter("email", email).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public UserEntity getUserByUserName(final String userName) {
        try {
            return entityManager.createNamedQuery("userByUserName", UserEntity.class).setParameter("userName", userName).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public UserEntity getuserByUuid(final String uuid) {
        try {
            return entityManager.createNamedQuery("userByUuid", UserEntity.class).setParameter("uuid", uuid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public UserAuth createAuthToken(final UserAuth userAuthTokenEntity) {
        entityManager.persist(userAuthTokenEntity);
        return userAuthTokenEntity;
    }

    public UserAuth getUserAuthByToken(final String authToken) {
        try {
            return entityManager.createNamedQuery("userAuthTokenByAccessToken", UserAuth.class).setParameter("accessToken", authToken).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public UserAuth getUserAuthByUser(final UserEntity userEntity) {
        try {
            return entityManager.createNamedQuery("userAuthTokenByUser", UserAuth.class).setParameter("user", userEntity).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public void updateUserAuth(final UserAuth updatedUserAuth) {
        entityManager.merge(updatedUserAuth);
    }
    //update User.

       public void updateUser(final UserEntity updatedUserEntity) {
        entityManager.merge(updatedUserEntity);
    }
    // update UserAuthEntity.

    public void updateUserAuthEntity(final UserAuth updatedUserAuthEntity) {
        entityManager.merge(updatedUserAuthEntity);
    }

    public void deleteUser(final UserEntity updatedUserEntity) {
        entityManager.remove(updatedUserEntity);
    }
}
