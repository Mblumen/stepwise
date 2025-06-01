package de.hd.fitbittracks.pojos;

import java.util.Objects;

import de.hd.fitbittracks.enums.ListItemType;

public class Separator implements ListItem{
    public String title;

    public Separator(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Separator that)) return false;

        return Objects.equals(title, that.title);
    }

    @Override
    public long getId() { return -1 * title.hashCode(); } // unique negative id for separators

    @Override
    public ListItemType getType() {
        return ListItemType.SEPARATOR;
    }
}
