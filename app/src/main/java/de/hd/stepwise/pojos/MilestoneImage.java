package de.hd.stepwise.pojos;

import java.util.Objects;

public class MilestoneImage {
    public String imageUrl;
    public String localImagePath;
    public String description;
    public long position;

    public MilestoneImage(String imageUrl, String description) {
        this.imageUrl = imageUrl;
        this.localImagePath = null;
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MilestoneImage that)) return false;

        if (!Objects.equals(imageUrl,that.imageUrl)) return false;
        if (!Objects.equals(localImagePath, that.localImagePath)) return false;
        if (position != that.position) return false;
        return description.equals(that.description);
    }
}