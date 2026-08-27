package de.hd.stepwise.pojos;

import java.util.Objects;

public class MilestoneDiscovery {
    public String title;
    public String text;
    public String sourceUrl;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof MilestoneDiscovery that)) return false;
        return Objects.equals(title, that.title)
                && Objects.equals(text, that.text)
                && Objects.equals(sourceUrl, that.sourceUrl);
    }
}
