package de.hd.stepwise.dtos;

import de.hd.stepwise.enums.RecordType;

public class RecordJson {
    public String name;
    public String unit;
    public RecordType type; // Type of the record, e.g., STEPS, DISTANCE, etc.

    public float value; // Optional, can be used for storing numeric values associated with the record
    public long trackId; // Optional, can be used to associate the record with a specific track
    public long timestamp; // Timestamp for when the record was created or last updated
    public String description; // Optional description for the record
}
