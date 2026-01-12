package de.hd.stepwise.pojos.events;

import de.hd.stepwise.entities.Achievement;

public class AchievementEvent extends MessageEvent<Achievement> {
    public AchievementEvent(Achievement content, String message) {
        super(content, message);
    }

    public AchievementEvent(Achievement content) {
        super(content);
    }
}
