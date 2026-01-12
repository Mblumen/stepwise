package de.hd.stepwise.dtos;

import java.util.List;

import de.hd.stepwise.pojos.TrackRoute;

public class TrackJson {
    public String name;
    public String startLocation;
    public String endLocation;
    public String imageUrl;
    public long challengeDuration;
    public TrackRoute trackRoute;
    public List<MilestoneJson> milestones;
}