package de.hd.fitbittracks.pojos;

import de.hd.fitbittracks.enums.ListItemType;

public interface ListItem {
    long getId();

    ListItemType getType();

    @Override
    boolean equals(Object o);
}