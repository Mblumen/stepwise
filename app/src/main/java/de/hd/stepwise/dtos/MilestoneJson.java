package de.hd.stepwise.dtos;

import java.util.List;

import de.hd.stepwise.pojos.MilestoneImage;
import de.hd.stepwise.pojos.MilestoneDiscovery;
import de.hd.stepwise.pojos.MilestoneQuiz;

public class MilestoneJson {
    public int distanceOffsetToPrevious;
    public String title;
    public String description;
    public String mapsUrl;
    public Double latitude;
    public Double longitude;
    public String imageUrl;
    public List<MilestoneImage> extraImages;
    public String audioUrl;
    public String stampImageUrl;
    public MilestoneDiscovery discovery;
    public MilestoneQuiz quiz;
}
