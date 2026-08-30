package de.hd.stepwise.ui.milestones;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;

import de.hd.stepwise.pojos.MilestoneQuiz;

public class MilestoneContentRulesTest {
    @Test
    public void absentAndMalformedQuizzesAreHidden() {
        assertFalse(MilestoneContentRules.validQuiz(null));
        MilestoneQuiz quiz = new MilestoneQuiz();
        quiz.question = "Question?";
        quiz.answers = List.of("Only one");
        quiz.correctAnswerIndex = 0;
        assertFalse(MilestoneContentRules.validQuiz(quiz));
    }

    @Test
    public void completeQuizIsVisible() {
        MilestoneQuiz quiz = new MilestoneQuiz();
        quiz.question = "Question?";
        quiz.answers = List.of("No", "Yes");
        quiz.correctAnswerIndex = 1;
        assertTrue(MilestoneContentRules.validQuiz(quiz));
    }
}
