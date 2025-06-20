package de.hd.fitbittracks.pojos.events;

import de.hd.fitbittracks.entities.Achievement;

public class AchievementEvent extends MessageEvent<Achievement> {
    public AchievementEvent(Achievement content, String message) {
        super(content, message);
    }

    public AchievementEvent(Achievement content) {
        super(content);
    }
}
