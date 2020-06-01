package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserSignupService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordCryptographyProvider passwordCryptographyProvider;

    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity signup(UserEntity userEntity) throws SignUpRestrictedException {
        UserEntity userByEmail = userDao.getUserByEmail(userEntity.getEmail());
        if(userByEmail != null){
           throw new SignUpRestrictedException("SGR-002","This user has already been registered, try with any other emailId");
        }
        UserEntity userByUserName = userDao.getUserByUserName(userEntity.getUserName());
        if(userByUserName != null){
            throw new SignUpRestrictedException("SGR-001","Try any other Username, this Username has already been taken");
        }

        String[] encryptedText = passwordCryptographyProvider.encrypt(userEntity.getPassword());
        userEntity.setSalt(encryptedText[0]);
        userEntity.setPassword(encryptedText[1]);

        return userDao.createUser(userEntity);
    }
}
