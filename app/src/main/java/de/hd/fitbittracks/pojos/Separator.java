package de.hd.fitbittracks.pojos;

import java.util.Objects;

import de.hd.fitbittracks.enums.ListItemType;

public class Separator<T> implements ListItem{
    public String title;

    public boolean isExpanded;
    public T data; // optional data, not used in this implementation
    private final Class<T> genericType;

    public Separator(String title, Class<T> genericType) {
        this.title = title;
        this.genericType = genericType;
        this.data = null; // default to null if no data is provided
    }

    public Separator(String title, T data, Class<T> genericType) {
        this.title = title;
        this.data = data; // allow setting data if needed
        this.genericType = genericType;
    }


    @Override
    public long getId() { return -1 * title.hashCode(); } // unique negative id for separators

    @Override
    public ListItemType getType() {
        return ListItemType.SEPARATOR;
    }
    public Class<T> getGenericType() {
        return genericType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Separator<?> that)) return false;
        return Objects.equals(title, that.title) && Objects.equals(data, that.data) && isExpanded == that.isExpanded;
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, isExpanded, data);
    }
}
