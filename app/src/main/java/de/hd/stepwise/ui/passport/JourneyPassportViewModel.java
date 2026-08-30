package de.hd.stepwise.ui.passport;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.AndroidViewModel;

import org.json.JSONException;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.hd.stepwise.pojos.JourneyPassport;
import de.hd.stepwise.repositories.JourneySummaryRepository;
import de.hd.stepwise.routing.RouteService;
import de.hd.stepwise.pojos.events.Event;

@HiltViewModel
public class JourneyPassportViewModel extends AndroidViewModel {
    private final JourneySummaryRepository repository;
    private final RouteService routeService;
    private final JourneyShareCardGenerator shareCardGenerator;
    private final MutableLiveData<List<GeoPoint>> route = new MutableLiveData<>();
    private final MutableLiveData<Event<Uri>> shareUri = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> shareFailed = new MutableLiveData<>();
    private final ExecutorService routeExecutor = Executors.newSingleThreadExecutor();

    @Inject
    public JourneyPassportViewModel(@NonNull Application application,
                                    JourneySummaryRepository repository,
                                    RouteService routeService,
                                    JourneyShareCardGenerator shareCardGenerator) {
        super(application);
        this.repository = repository;
        this.routeService = routeService;
        this.shareCardGenerator = shareCardGenerator;
    }

    public LiveData<JourneyPassport> passport(long progressId) {
        return Transformations.map(repository.observeJourney(progressId), JourneyPassport::from);
    }

    public LiveData<List<GeoPoint>> route() { return route; }
    public LiveData<Event<Uri>> shareUri() { return shareUri; }
    public LiveData<Event<Boolean>> shareFailed() { return shareFailed; }

    public void loadRoute(JourneyPassport passport) {
        routeExecutor.execute(() -> {
            try {
                route.postValue(routeService.getGeoData(passport.track.trackRoute));
            } catch (JSONException | IOException exception) {
                route.postValue(List.of());
            }
        });
    }

    public void share(JourneyPassport passport) {
        shareCardGenerator.generate(passport, uri -> shareUri.postValue(new Event<>(uri)),
                exception -> shareFailed.postValue(new Event<>(true)));
    }

    @Override protected void onCleared() { routeExecutor.shutdownNow(); }
}
