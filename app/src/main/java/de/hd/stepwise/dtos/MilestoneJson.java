package de.hd.stepwise.dtos;

import java.util.List;

import de.hd.stepwise.pojos.MilestoneImage;

public class MilestoneJson {
    public int distanceOffsetToPrevious;
    public String title;
    public String description;
    public String mapsUrl;
    public Double latitude;
    public Double longitude;
    public String imageUrl;
    public List<MilestoneImage> extraImages;
}