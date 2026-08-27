package de.hd.stepwise.helper;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

import de.hd.stepwise.dtos.MilestoneJson;
import de.hd.stepwise.dtos.TrackJson;
import de.hd.stepwise.pojos.MilestoneDiscovery;
import de.hd.stepwise.pojos.MilestoneQuiz;

public class TrackCatalogParser {
    private final Gson gson = new Gson();

    public TrackJson parse(String json) {
        JsonObject source = JsonParser.parseString(json).getAsJsonObject();
        JsonObject base = source.deepCopy();
        JsonArray sourceMilestones = array(source, "milestones");
        JsonArray baseMilestones = array(base, "milestones");
        for (JsonElement element : baseMilestones) {
            if (!element.isJsonObject()) continue;
            JsonObject milestone = element.getAsJsonObject();
            milestone.remove("audioUrl");
            milestone.remove("stampImageUrl");
            milestone.remove("discovery");
            milestone.remove("quiz");
        }

        TrackJson track = gson.fromJson(base, TrackJson.class);
        if (track.milestones == null) track.milestones = new ArrayList<>();
        int richContentCount = Math.min(track.milestones.size(), sourceMilestones.size());
        for (int index = 0; index < richContentCount; index++) {
            JsonElement element = sourceMilestones.get(index);
            if (element.isJsonObject()) {
                applyOptionalContent(track.milestones.get(index), element.getAsJsonObject());
            }
        }
        return track;
    }

    private void applyOptionalContent(MilestoneJson milestone, JsonObject source) {
        milestone.audioUrl = optionalNonBlankString(source.get("audioUrl"));
        milestone.stampImageUrl = optionalNonBlankString(source.get("stampImageUrl"));
        milestone.discovery = validDiscovery(source.get("discovery"));
        milestone.quiz = validQuiz(source.get("quiz"));
    }

    private MilestoneDiscovery validDiscovery(JsonElement element) {
        try {
            if (element == null || !element.isJsonObject()) return null;
            MilestoneDiscovery discovery = gson.fromJson(element, MilestoneDiscovery.class);
            if (discovery == null || isBlank(discovery.title) || isBlank(discovery.text)) return null;
            discovery.title = discovery.title.trim();
            discovery.text = discovery.text.trim();
            discovery.sourceUrl = trimToNull(discovery.sourceUrl);
            return discovery;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private MilestoneQuiz validQuiz(JsonElement element) {
        try {
            if (element == null || !element.isJsonObject()) return null;
            MilestoneQuiz quiz = gson.fromJson(element, MilestoneQuiz.class);
            if (quiz == null || isBlank(quiz.question) || quiz.answers == null
                    || quiz.answers.size() < 2 || quiz.correctAnswerIndex < 0
                    || quiz.correctAnswerIndex >= quiz.answers.size()) {
                return null;
            }
            List<String> answers = new ArrayList<>();
            for (String answer : quiz.answers) {
                if (isBlank(answer)) return null;
                answers.add(answer.trim());
            }
            quiz.question = quiz.question.trim();
            quiz.answers = answers;
            quiz.explanation = trimToNull(quiz.explanation);
            return quiz;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static JsonArray array(JsonObject object, String name) {
        JsonElement element = object.get(name);
        return element != null && element.isJsonArray() ? element.getAsJsonArray() : new JsonArray();
    }

    private static String optionalNonBlankString(JsonElement element) {
        try {
            return element != null && element.isJsonPrimitive()
                    ? trimToNull(element.getAsString()) : null;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
