package de.hd.stepwise.ui;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import de.hd.stepwise.database.AppDatabase;
import de.hd.stepwise.enums.ResultStatus;
import de.hd.stepwise.helper.DataInitializer;
import de.hd.stepwise.helper.InitPrefs;
import de.hd.stepwise.pojos.MethodResult;
import de.hd.stepwise.pojos.events.Event;

public class UpdateViewModel extends AndroidViewModel {
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Event<MethodResult>> _updateResult = new MutableLiveData<>();
    public LiveData<Event<MethodResult>> updateResult = _updateResult;
    private boolean running = false;
    protected final AppDatabase db;
    public UpdateViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
    }
    public LiveData<Boolean> isLoading() {
        return loading;
    }
    public void initialize(Context context, AppDatabase db, DataInitializer.DataType type) {
        if (InitPrefs.isInitialized(context)) {
            return; // already done → do nothing
        }
        update(context, type);
    }

    public void update(Context context, DataInitializer.DataType type) {
        if (running) return;
        running = true;
        loading.postValue(true);
        DataInitializer.updateDataset(context, db, type,
                () -> {
                    InitPrefs.markInitialized(context);
                    loading.postValue(false);
                    running = false;
                },
                (String updateMessage) -> {
                    _updateResult.postValue(new Event<>(new MethodResult(ResultStatus.SUCCESS, updateMessage)));
                },
                () -> {
                    _updateResult.postValue(new Event<>(new MethodResult(ResultStatus.ERROR, "Update failed")));
                },
                () -> {
                    _updateResult.postValue(new Event<>(new MethodResult(ResultStatus.SUCCESS, "Already up to date")));
                }
        );
    }
}
