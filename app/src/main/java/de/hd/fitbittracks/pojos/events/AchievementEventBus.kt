package de.hd.fitbittracks.pojos.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object AchievementEventBus {
    private val _achievementEvents = MutableSharedFlow<AchievementEvent>(extraBufferCapacity = 1)
    val achievementEvents: SharedFlow<AchievementEvent> = _achievementEvents.asSharedFlow()

    fun postEvent(event: AchievementEvent) {
        _achievementEvents.tryEmit(event)
    }
}