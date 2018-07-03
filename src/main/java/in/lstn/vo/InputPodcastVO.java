package in.lstn.vo;

import java.io.Serializable;

public class InputPodcastVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
	public String category;
    public String feedUrl;
    public String name;
    public String[] genres;
    public String countryCode;
    public boolean isPopular;

    public InputPodcastVO(){};
}