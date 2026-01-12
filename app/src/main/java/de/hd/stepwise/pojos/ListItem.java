package de.hd.stepwise.pojos;

import de.hd.stepwise.enums.ListItemType;

public interface ListItem {
    long getId();

    ListItemType getType();

    @Override
    boolean equals(Object o);
}