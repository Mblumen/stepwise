package de.hd.stepwise.repositories;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.hd.stepwise.daos.StepEventDao;
import de.hd.stepwise.database.AppDatabase;
import de.hd.stepwise.entities.StepEvent;
import de.hd.stepwise.enums.StepSource;

@Singleton
public class StepEventRepository {

    private final StepEventDao stepEventDao;
    @Inject
    public StepEventRepository(AppDatabase db) {
        stepEventDao = db.stepEventDao();
    }

    public StepEvent addStepEvent(StepEvent event) {
        long id = stepEventDao.insertStepEvent(event);
        event.id = id;
        return event;
    }

    public List<StepEvent> getUnhandledStepEvents(StepSource source) {
        return stepEventDao.getUnhandledStepEvents(source);
    }

    public void markEventHandled(long eventId) {
        stepEventDao.markStepEventAsHandled(eventId);
    }
    public void markEventsHandled(List<Long> eventIds) {
        stepEventDao.markStepEventsAsHandled(eventIds);
    }
}
