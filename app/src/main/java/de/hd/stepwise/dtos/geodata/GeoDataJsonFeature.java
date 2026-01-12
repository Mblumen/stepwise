package de.hd.stepwise.dtos.geodata;

import java.util.List;

public class GeoDataJsonFeature {
    public String type;
    public List<Double> bbox;
    public GeoDataJsonProperties properties;
    public GeoDataJsonGeometry geometry;
}
