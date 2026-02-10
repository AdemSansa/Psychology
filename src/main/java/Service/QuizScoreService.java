package Service;


import DAO.QuizResultDTO;
import Entities.Answer;

import java.util.List;

import static Entities.QuizCategory.*;

public class QuizScoreService {

    public QuizResultDTO calculateScores(List<Answer> answers) {

        int stress = 0;
        int anxiety = 0;
        int depression = 0;
        int burnout = 0;

        for (Answer answer : answers) {
            switch (answer.getCategory()) {
                case STRESS -> stress += answer.getValue();
                case ANXIETY -> anxiety += answer.getValue();
                case DEPRESSION -> depression += answer.getValue();
                case BURNOUT -> burnout += answer.getValue();
            }
        }

        return new QuizResultDTO(
                clamp(stress),
                clamp(anxiety),
                clamp(depression),
                clamp(burnout)
        );
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(value, 20));
    }
}
