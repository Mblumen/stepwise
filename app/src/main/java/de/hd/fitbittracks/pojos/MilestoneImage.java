package de.hd.fitbittracks.pojos;

public class MilestoneImage {
    public String imagePath;
    public String description;

    public MilestoneImage(String imagePath, String description) {
        this.imagePath = imagePath;
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MilestoneImage that)) return false;

        if (!imagePath.equals(that.imagePath)) return false;
        return description.equals(that.description);
    }
}