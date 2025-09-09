package com.backend.topperfriendweb.service;

import com.backend.topperfriendweb.model.Quiz;
import com.backend.topperfriendweb.repository.QuizRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuizService {
    private final QuizRepository quizRepository;

    public QuizService(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    public List<Quiz> saveAll(List<Quiz> quizzes) {
        return quizRepository.saveAll(quizzes);
    }

    public List<Quiz> getQuizzesByUser(Long userId) {
        return quizRepository.findByUserId(userId);
    }
}
