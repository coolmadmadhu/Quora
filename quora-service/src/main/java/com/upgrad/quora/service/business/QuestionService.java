package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.Questions;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuestionService {

    @Autowired
    private QuestionDao questionDao;

    @Autowired
    private UserDao userDao;

    // Create a question

    @Transactional(propagation = Propagation.REQUIRED)
    public Questions createQuestion(Questions questionEntity)
            throws InvalidQuestionException {
        String content = questionEntity.getContent();
        if (content == null || content.isEmpty() || content.trim().isEmpty()) {
            throw new InvalidQuestionException("QUE-888", "Content can't be null or empty");
        }

        if (questionDao.getQuestionByContent(content.trim()) != null) {
            throw new InvalidQuestionException("QUE-999",
                    "Question already exists. Duplicate question not allowed");
        }

        return questionDao.createQuestion(questionEntity);
    }

    // edit a question.

    @Transactional(propagation = Propagation.REQUIRED)
    public Questions editQuestion(String content, String userUuid, String questionuuid)
            throws AuthorizationFailedException, InvalidQuestionException {
        Questions questionEntity = questionDao.getQuestion(questionuuid);
        if (questionEntity == null) {
            throw new InvalidQuestionException("QUES-001", "Entered question uuid does not exist");
        }
        if (userUuid != null && !userUuid.equals(questionEntity.getUser().getUuid())) {
            throw new AuthorizationFailedException("ATHR-003",
                    "Only the question owner can edit the question");
        }
        if (content == null || content.isEmpty() || content.trim().isEmpty() || content
                .equalsIgnoreCase(questionEntity.getContent())) {
            throw new InvalidQuestionException("QUE-888",
                    "Content can't be null or empty or equal to existing content");
        }
        questionEntity.setContent(content);
        questionDao.updateQuestion(questionEntity);
        return questionDao.getQuestion(questionuuid);
    }

    public List<Questions> getAllQuestions() {
        return questionDao.findAll();
    }

    // get question by uuid.

    public Questions getQuestionById(String questionuuid) throws InvalidQuestionException {
        Questions questionEntity = questionDao.getQuestion(questionuuid);
        if (questionEntity == null) {
            throw new InvalidQuestionException("QUES-001", "The question entered is invalid");
        }
        return questionEntity;
    }

    // get all questions by a particular user.

    public List<Questions> getAllQuestionsByUser(String uuid) throws UserNotFoundException {
        UserEntity user = userDao.getuserByUuid(uuid);
        if (user == null) {
            throw new UserNotFoundException("USR-001",
                    "User with entered uuid whose question details are to be seen does not exist");
        }
        return questionDao.findAllByUser(user);
    }

    //delete a question.

    @Transactional(propagation = Propagation.REQUIRED)
    public Questions deleteQuestion(UserEntity user, String questionuuid)
            throws AuthorizationFailedException, InvalidQuestionException {
        Questions questionEntity = questionDao.getQuestion(questionuuid);
        if (questionEntity == null) {
            throw new InvalidQuestionException("QUES-001", "Entered question uuid does not exist");
        }

        if (!user.getUuid().equals(questionEntity.getUser().getUuid()) && !user.getRole()
                .equalsIgnoreCase("admin")) {
            throw new AuthorizationFailedException("ATHR-003",
                    "Only the question owner or admin can delete the question");
        }
        return questionDao.deleteQuestion(questionEntity);
    }
}
