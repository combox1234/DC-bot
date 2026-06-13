package com.placementcompanion.bot.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.placementcompanion.bot.model.Question;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private List<Question> allQuestions = new ArrayList<>();
    private final Random random = new Random();
    private File questionsFile;

    @PostConstruct
    public void loadQuestions() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            questionsFile = new File("../placement-companion/data/questions.json");
            if (questionsFile.exists()) {
                allQuestions = mapper.readValue(questionsFile, new TypeReference<List<Question>>(){});
                System.out.println("Loaded " + allQuestions.size() + " questions from JSON.");
            } else {
                System.err.println("WARNING: questions.json not found at " + questionsFile.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Question getRandomQuestion(String category) {
        if (allQuestions.isEmpty()) return null;

        List<Question> filtered = allQuestions;
        if (category != null && !category.equalsIgnoreCase("any")) {
            filtered = allQuestions.stream()
                    .filter(q -> q.getCategory().equalsIgnoreCase(category))
                    .collect(Collectors.toList());
        }

        if (filtered.isEmpty()) return null;
        return filtered.get(random.nextInt(filtered.size()));
    }

    public int getTotalQuestionCount() {
        return allQuestions.size();
    }

    public synchronized boolean addQuestion(String category, String questionText, List<String> options, int answer, String explanation) {
        int newId = allQuestions.stream().mapToInt(Question::getId).max().orElse(0) + 1;
        Question q = new Question();
        q.setId(newId);
        q.setCategory(category);
        q.setQuestion(questionText);
        q.setOptions(options);
        q.setAnswer(answer);
        q.setExplanation(explanation);
        allQuestions.add(q);
        return saveToFile();
    }

    public synchronized boolean removeQuestion(int questionId) {
        boolean removed = allQuestions.removeIf(q -> q.getId() == questionId);
        if (removed) {
            saveToFile();
        }
        return removed;
    }

    private boolean saveToFile() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(questionsFile, allQuestions);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}

