package de.hd.stepwise.repositories;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BaseRepository {
    protected final Executor executor = Executors.newSingleThreadExecutor();
}
