package de.hd.fitbittracks.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.DatabaseView;
import androidx.room.PrimaryKey;

import java.util.List;
import java.util.Objects;

import de.hd.fitbittracks.pojos.MilestoneImage;
import de.hd.fitbittracks.ui.milestones.MilestoneItem;

@DatabaseView("""
  SELECT
    m.*,
    SUM(m.distanceOffsetToPrevious) OVER (
      PARTITION BY m.trackId
      ORDER BY m.id
      ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
    ) AS totalDistance
  FROM milestones m
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
    public String image; // optional
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
        return Objects.equals(image, that.image);
    }
}