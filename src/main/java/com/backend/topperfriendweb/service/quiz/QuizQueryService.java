package com.backend.topperfriendweb.service.quiz;

import com.backend.topperfriendweb.dto.quiz.QuizDTO;
import com.backend.topperfriendweb.model.Quiz;
import com.backend.topperfriendweb.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuizQueryService {
    private final QuizRepository quizRepository;

    public QuizQueryService(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    @Transactional(readOnly = true)
    public List<QuizDTO> getUserQuizzes(Long userId) {
        return quizRepository.findByUserId(userId)
                .stream()
                .map(QuizDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public QuizDTO getQuizById(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        return new QuizDTO(quiz);
    }
}
