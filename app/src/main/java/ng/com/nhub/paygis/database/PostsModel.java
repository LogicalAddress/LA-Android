package ng.com.nhub.paygis.database;

/**
 * Created by retnan on 4/12/16.
 */
public class PostsModel {

    public Long _id;
    public String newsId;
    public String title;
    public String content;
    public Integer contentType;
    public String contentImagePath;
    public String logo;
    public Integer nPollItems;
    public String jsonPollParams;
    public Boolean pollExpires;
    public String pollExpiresTime;

    //TODO: everypost has a timestamp. add!

    public PostsModel(String newsId, String title, String content, Integer contentType,
                      String contentImagePath, String logo) {
        this.title = title;
        this.newsId = newsId;
        this.content = content;
        this.contentType = contentType;
        this.contentImagePath = contentImagePath;
        this.logo = logo;
    }

    public PostsModel(String newsId, String title, String content, Integer contentType,
                      String contentImagePath, String logo, Integer nPollItems,
                      String jsonPollParams, Boolean pollExpires, String pollExpiresTime) {
        this.title = title;
        this.newsId = newsId;
        this.content = content;
        this.contentType = contentType;
        this.contentImagePath = contentImagePath;
        this.logo = logo;
        this.nPollItems = nPollItems;
        this.jsonPollParams = jsonPollParams;
        this.pollExpires = pollExpires;
        this.pollExpiresTime = pollExpiresTime;
    }
}
