package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    @Autowired
    private UserDao userDao;

    @Transactional
    public UserEntity deleteUser(final String uuid, final String authorization) throws AuthorizationFailedException, UserNotFoundException {
        UserEntity userByID = userDao.getuserByUuid(uuid);
        UserAuthEntity userAuth = userDao.getUserAuthByToken(authorization);
        if(userAuth == null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }
        if(userAuth.getLogoutAt() != null){
            throw new AuthorizationFailedException("ATHR-002","User is signed out");
        }
        if(userByID == null){
            throw new UserNotFoundException("USR-001","User with entered uuid does not exist");
        }
        if(userAuth.getUser().getRole().equals("nonadmin")){
            throw new AuthorizationFailedException("ATHR-003","Unauthorized Access, Entered user is not an admin");
        }
        userDao.deleteUser(userByID);
        return userByID;
    }

}
