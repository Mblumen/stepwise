package de.hd.stepwise.entities;

import androidx.room.ColumnInfo;
import androidx.room.DatabaseView;

import java.util.List;
import java.util.Objects;

import de.hd.stepwise.pojos.MilestoneImage;
import de.hd.stepwise.ui.milestones.MilestoneItem;

@DatabaseView("""
  SELECT
    m.*,
    SUM(m.distanceOffsetToPrevious) OVER (
      PARTITION BY m.trackId
      ORDER BY m.id
      ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
    ) AS totalDistance
  FROM milestone m
""")
public class MilestoneWithTotalDistance implements MilestoneItem {
    public long id;
    public long trackId;
    public int distanceOffsetToPrevious;
    public int totalDistance;
    public String title = "";
    public String description = "";
    public String mapsUrl;
    public double latitude;
    public double longitude;
    public String imageUrl; // optional
    public String localImagePath; // optional
    public boolean unlocked = false; // default is locked
    @ColumnInfo(name = "extra_images")
    public List<MilestoneImage> extraImages;

    @Override
    public MilestoneWithTotalDistance getMilestone() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Milestone that)) return false;

        if (id != that.id) return false;
        if (trackId != that.trackId) return false;
        if (!title.equals(that.title)) return false;
        if (!description.equals(that.description)) return false;
        if (!Objects.equals(imageUrl, that.imageUrl)) return false;
        return Objects.equals(localImagePath, that.localImagePath);
    }
}