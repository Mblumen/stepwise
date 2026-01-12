package de.hd.stepwise.dtos.geodata;

import java.util.List;

public class GeoDataJson {
    public String type;
    public List<Double> bbox;
    public List<GeoDataJsonFeature> features;
}
