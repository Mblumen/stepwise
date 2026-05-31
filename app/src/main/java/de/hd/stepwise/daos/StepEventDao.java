package de.hd.stepwise.daos;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Upsert;

import java.util.List;

import de.hd.stepwise.entities.StepEvent;
import de.hd.stepwise.entities.Track;
import de.hd.stepwise.enums.StepSource;

@Dao
public interface StepEventDao {
    @Upsert
    long insertStepEvent(StepEvent stepEvent);

    @Query("SELECT * FROM step_event WHERE handled = 0 and source = :source")
    List<StepEvent> getUnhandledStepEvents(StepSource source);

    @Query("UPDATE step_event SET handled = true WHERE id = :id")
    void markStepEventAsHandled(long id);
    @Query("UPDATE step_event SET handled = true WHERE id IN (:ids)")
    void markStepEventsAsHandled(List<Long> ids);
}
