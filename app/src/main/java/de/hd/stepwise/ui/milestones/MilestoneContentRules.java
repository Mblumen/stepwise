package de.hd.stepwise.ui.milestones;

import de.hd.stepwise.pojos.MilestoneQuiz;

final class MilestoneContentRules {
    private MilestoneContentRules() { }

    static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    static boolean validQuiz(MilestoneQuiz quiz) {
        return quiz != null && hasText(quiz.question) && hasText(quiz.explanation)
                && quiz.answers != null
                && quiz.answers.size() >= 2
                && quiz.answers.stream().allMatch(MilestoneContentRules::hasText)
                && quiz.correctAnswerIndex >= 0
                && quiz.correctAnswerIndex < quiz.answers.size();
    }
}
