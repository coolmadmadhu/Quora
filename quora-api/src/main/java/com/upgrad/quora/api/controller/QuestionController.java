package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.AuthenticationService;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.entity.Questions;
import com.upgrad.quora.service.entity.UserAuth;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class QuestionController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private QuestionService questionService;

    // Create a question. It requires the authorization validation and question request is submitted in the database //

    @RequestMapping(method = RequestMethod.POST, path = "/question/create",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionResponse> postQuestion(
            @RequestHeader("authorization") final String authorization,
            final QuestionRequest questionRequest)
            throws AuthorizationFailedException, AuthenticationFailedException, InvalidQuestionException {

        //Get the access token
        String accessToken = authenticationService.getBearerAccessToken(authorization);

        //Validate the authentication
        UserAuth userAuth = authenticationService
                .validateBearerAuthentication(accessToken, "for posting a question");

        //Get the user details and fill question detail, associate user
        UserEntity user = userAuth.getUser();
        Questions questionEntity = new Questions();
        questionEntity.setUuid(UUID.randomUUID().toString());
        questionEntity.setContent(questionRequest.getContent());
        questionEntity.setDate(ZonedDateTime.now());
        questionEntity.setUser(user);

        //Invoke business service to create question. If same question already exists,
        // throw DuplicateQuestion related Exception message
        questionService.createQuestion(questionEntity);
        QuestionResponse questionResponse = new QuestionResponse().id(questionEntity.getUuid())
                .status("QUESTION CREATED");
        return new ResponseEntity<QuestionResponse>(questionResponse, HttpStatus.OK);
    }

    // Edit a question. It requires validation of question uuid and authorization token as mandetory input fields//

    @RequestMapping(method = RequestMethod.PUT, path = "/question/edit/{questionId}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionEditResponse> editQuestion(
            @RequestHeader("authorization") final String authorization,
            @PathVariable("questionId") final String questionId,
            final QuestionEditRequest questionEditRequest)
            throws AuthorizationFailedException, InvalidQuestionException, AuthenticationFailedException {

        //Get access token
        String accessToken = authenticationService.getBearerAccessToken(authorization);

        //Validate authentication token
        UserAuth userAuth = authenticationService
                .validateBearerAuthentication(accessToken, "to edit the question");
        UserEntity user = userAuth.getUser();

        //Invoke business Service to edit the question
        Questions questionEntity = questionService
                .editQuestion(questionEditRequest.getContent(), user.getUuid(), questionId);
        QuestionEditResponse questionEditResponse = new QuestionEditResponse()
                .id(questionEntity.getUuid()).status("QUESTION EDITED");
        return new ResponseEntity<QuestionEditResponse>(questionEditResponse, HttpStatus.OK);
    }

    // all questions in database

    @RequestMapping(method = RequestMethod.GET, path = "/question/all",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestions(
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, AuthenticationFailedException {

        //Get access token
        String accessToken = authenticationService.getBearerAccessToken(authorization);

        //Validate authentication token
        UserAuth userAuth = authenticationService
                .validateBearerAuthentication(accessToken, "to get all questions");

        //Invoke business Service to get all the questions and add them to a collection
        // and send across in ResponseEntity
        List<Questions> questionEntityList = questionService.getAllQuestions();
        return getListResponseEntity(questionEntityList);
    }

    // Get questions of a particular user. It requires authorization, userid to be validated//

    @RequestMapping(method = RequestMethod.GET, path = "/question/all/{userId}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestionsByUser(
            @RequestHeader("authorization") final String authorization,
            @PathVariable("userId") final String userId)
            throws AuthenticationFailedException, AuthorizationFailedException, UserNotFoundException {
        String accessToken = authenticationService.getBearerAccessToken(authorization);
        UserAuth userAuth = authenticationService
                .validateBearerAuthentication(accessToken, "to get all questions by user");
        UserEntity user = userAuth.getUser();
        List<Questions> questionEntityList = questionService.getAllQuestionsByUser(userId);
        return getListResponseEntity(questionEntityList);

    }

    // Preparing list of QuestionDetails.

    private ResponseEntity<List<QuestionDetailsResponse>> getListResponseEntity(
            List<Questions> questionEntityList) {
        List<QuestionDetailsResponse> ent = new ArrayList<QuestionDetailsResponse>();
        for (Questions n : questionEntityList) {
            QuestionDetailsResponse questionDetailsResponse = new QuestionDetailsResponse();
            questionDetailsResponse.id(n.getUuid());
            questionDetailsResponse.content(n.getContent());
            ent.add(questionDetailsResponse);
        }

        return new ResponseEntity<List<QuestionDetailsResponse>>(ent, HttpStatus.OK);
    }

    // Delete a question. It also validates the autorization, question uuid and then deletes the entry from database.

    @RequestMapping(method = RequestMethod.DELETE, path = "/question/delete/{questionId}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionDeleteResponse> deleteQuestion(
            @RequestHeader("authorization") final String authorization,
            @PathVariable("questionId") final String questionId)
            throws AuthenticationFailedException, AuthorizationFailedException, InvalidQuestionException {
        String accessToken = authenticationService.getBearerAccessToken(authorization);
        UserAuth userAuth = authenticationService
                .validateBearerAuthentication(accessToken, "to delete the question");
        UserEntity user = userAuth.getUser();
        Questions questionEntity = questionService.deleteQuestion(user, questionId);
        QuestionDeleteResponse deleteResponse = new QuestionDeleteResponse()
                .id(questionEntity.getUuid()).status("QUESTION DELETED");
        return new ResponseEntity<QuestionDeleteResponse>(deleteResponse, HttpStatus.OK);
    }
}
