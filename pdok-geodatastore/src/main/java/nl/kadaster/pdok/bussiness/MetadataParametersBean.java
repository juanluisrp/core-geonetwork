package nl.kadaster.pdok.bussiness;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by JuanLuis on 13/07/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetadataParametersBean {
    private String title;
    private String summary;
    private List<String> keywords;
    private List<String> topicCategories;
    private String location;
    private String lineage;
    private String useLimitation;
    private String license;
    private String resolution;
    private String identifier;
    private String url;
    private String extent;
    private boolean error;
    private List<String> messages;
    private String status;
    private String fileType;

    public MetadataParametersBean() {
        this.keywords = Lists.newArrayList();
        this.topicCategories = Lists.newArrayList();
        this.messages = Lists.newArrayList();
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getTopicCategories() {
        return topicCategories;
    }

    public void setTopicCategories(List<String> topicCategories) {
        this.topicCategories = topicCategories;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLineage() {
        return lineage;
    }

    public void setLineage(String lineage) {
        this.lineage = lineage;
    }

    public String getUseLimitation() {
        return useLimitation;
    }

    public void setUseLimitation(String useLimitation) {
        this.useLimitation = useLimitation;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    /**
     * The identifier of the dataset.
     */
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * The URL to the dataset.
     */
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * The extent of the location (WKT).
     */
    public String getExtent() {
        return extent;
    }

    public void setExtent(String extent) {
        this.extent = extent;
    }

    /**
     * true if successful
     */
    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    /**
     * If <code>error</code> is <false />, a list of error messages.
     */
    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    /**
     * Published/archived status of the dataset.
     */
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "MetadataParametersBean{" +
                "identifier='" + identifier + '\'' +
                ", title='" + title + '\'' +
                '}';
    }

    public void addMessage(String message) {
        this.messages.add(message);
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileType() {
        return fileType;
    }
}
