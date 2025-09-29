package com.backend.topperfriendweb.service.quiz;

import com.backend.topperfriendweb.dto.quiz.QuizDTO;
import com.backend.topperfriendweb.dto.quiz.QuizSubmissionResponse;
import com.backend.topperfriendweb.dto.quiz.SubmitQuizRequest;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizService {
    private final QuizQueryService quizQueryService;
    private final QuizMutationService quizMutationService;
    private final QuizRepository quizRepository;

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
        // Enforce 24-hour per-user limit (2)
        int limit = 2;
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        long used = quizRepository.countByUserIdAndCreatedAtAfter(user.getId(), since);
        if (used >= limit) {
            // Compute reset time based on oldest within window
            LocalDateTime resetAt = quizRepository
                    .findFirstByUserIdAndCreatedAtAfterOrderByCreatedAtAsc(user.getId(), since)
                    .map(q -> q.getCreatedAt().plusHours(24))
                    .orElse(LocalDateTime.now().plusHours(24));
            throw new IllegalArgumentException("Daily quiz generation limit reached. Try again at: " + resetAt);
        }
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