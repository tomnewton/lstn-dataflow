package in.lstn.vo;

import java.util.HashMap;
import java.util.Map;

public class OutputPodcastVO {
    public String category;
    public String feedUrl;
    public String name;
    public String[] genres;
    public Map<String, CountryInfo> countryInfo;

    OutputPodcastVO(String category, String feedUrl, String name, String[] genres, String[] countryCodes) {
        this.category = category;
        this.feedUrl = feedUrl;
        this.name = name;
        this.genres = genres;
    }

    public OutputPodcastVO(){
        countryInfo = new HashMap<String, CountryInfo>();
    }

    public static class CountryInfo {
        public final boolean isPopular;
        public final boolean isPublished;

        public CountryInfo(boolean isPopular, boolean isPublished) {
            this.isPopular = isPopular;
            this.isPublished = isPublished;
        }
    }
}