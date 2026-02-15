package interfaces;

import Entities.Quiz;

public interface QuizListener {
    void onEdit(Quiz quiz);

    void onDelete(Quiz quiz);
}
