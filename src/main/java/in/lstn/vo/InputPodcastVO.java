package in.lstn.vo;

import java.io.Serializable;

public class InputPodcastVO implements Serializable {
    private static final long serialVersionUID = -4792845514952939742L;
	public String category;
    public String feedUrl;
    public String name;
    public String[] genres;
    public String countryCode;
    public boolean isPopular;

    InputPodcastVO(String category, String feedUrl, String name, String[] genres, String countryCode, boolean isPopular ) {
        this.category = category;
        this.feedUrl = feedUrl;
        this.name = name;
        this.genres = genres;
        this.countryCode = countryCode;
        this.isPopular = isPopular;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) {
            return true;
        }
        if ( obj instanceof InputPodcastVO == false ) {
            return false;
        }

        InputPodcastVO other = (InputPodcastVO) obj;
        return this.feedUrl.equals(other.feedUrl) && 
        this.category.equals(other.category) &&
        this.name.equals(other.name) &&
        this.genres.equals(other.genres) &&
        this.countryCode.equals(other.countryCode) &&
        this.isPopular == other.isPopular;
    }

    public InputPodcastVO(){};
}