package in.lstn.vo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class OutputPodcastVO implements Serializable {
    private static final long serialVersionUID = 1L;

    public String category;
    public String feedUrl;
    public String name;
    public String[] genres;
    public Map<String, CountryInfoVO> countryInfo;

    public OutputPodcastVO(){
        countryInfo = new HashMap<String, CountryInfoVO>();
    }

    public static class CountryInfoVO implements Serializable {
        private static final long serialVersionUID = 1L;
        
        public boolean isPopular;
        public boolean isPublished;

        public CountryInfoVO(boolean isPublished, boolean isPopular) {
            this.isPopular = isPopular;
            this.isPublished = isPublished;
        }
    }
}