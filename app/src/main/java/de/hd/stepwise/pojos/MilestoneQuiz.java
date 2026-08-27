package de.hd.stepwise.pojos;

import java.util.List;
import java.util.Objects;

public class MilestoneQuiz {
    public String question;
    public List<String> answers;
    public int correctAnswerIndex;
    public String explanation;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof MilestoneQuiz that)) return false;
        return correctAnswerIndex == that.correctAnswerIndex
                && Objects.equals(question, that.question)
                && Objects.equals(answers, that.answers)
                && Objects.equals(explanation, that.explanation);
    }
}
