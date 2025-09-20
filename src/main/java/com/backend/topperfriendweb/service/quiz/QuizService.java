package com.backend.topperfriendweb.service.quiz;

import com.backend.topperfriendweb.dto.quiz.QuizDTO;
import com.backend.topperfriendweb.dto.quiz.QuizSubmissionResponse;
import com.backend.topperfriendweb.dto.quiz.SubmitQuizRequest;
import com.backend.topperfriendweb.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizService {
    private final QuizQueryService quizQueryService;
    private final QuizMutationService quizMutationService;

    @Transactional(readOnly = true)
    public List<QuizDTO> getUserQuizzes(Long userId) {
        return quizQueryService.getUserQuizzes(userId);
    }

    @Transactional(readOnly = true)
    public QuizDTO getQuizById(Long quizId) {
        return quizQueryService.getQuizById(quizId);
    }

    @Transactional
    public QuizDTO createQuiz(String title, String questionsJson, User user) {
        return quizMutationService.createQuiz(title, questionsJson, user);
    }

    @Transactional
    public QuizSubmissionResponse submitQuiz(SubmitQuizRequest request, User user) {
        return quizMutationService.submitQuiz(request, user);
    }

    @Transactional
    public void deleteQuiz(Long quizId, Long userId) {
        quizMutationService.deleteQuiz(quizId, userId);
    }
}