package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.Questions;
import com.upgrad.quora.service.entity.UserEntity;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class QuestionDao {

    @PersistenceContext
    private EntityManager entityManager;

    // create a question.

    public Questions createQuestion(Questions questionEntity) {
        entityManager.persist(questionEntity);
        return questionEntity;
    }


    // Get question by uuid.

    public Questions getQuestion(final String questionuuid) {
        try {
            return entityManager.createNamedQuery("QuestionEntityByUuid", Questions.class)
                    .setParameter("uuid", questionuuid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    // Get question by content.

    public Questions getQuestionByContent(final String content) {
        try {
            return entityManager.createNamedQuery("QuestionEntityByContent", Questions.class)
                    .setParameter("content", content).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    // update a question.

    public Questions updateQuestion(final Questions questionEntity) {
        return entityManager.merge(questionEntity);
    }

    // Find all the questions in the system.

    public List<Questions> findAll() {
        return entityManager.createQuery("SELECT a FROM Questions a", Questions.class)
                .getResultList();
    }

    // find all questions posted by a particular user.

    public List<Questions> findAllByUser(UserEntity user) {
        return entityManager.createNamedQuery("QuestionEntitiesByUser", Questions.class)
                .setParameter("user", user).getResultList();
    }

    // Delete a question.

    @OnDelete(action = OnDeleteAction.CASCADE)
    public Questions deleteQuestion(final Questions questionEntity) {
        entityManager.remove(questionEntity);
        return questionEntity;
    }

}
