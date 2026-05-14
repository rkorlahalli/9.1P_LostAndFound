package com.example.lostfoundapp.data;

public class LostFoundItem {

    private int id;
    private String postType;
    private String name;
    private String phone;
    private String description;
    private String dateText;
    private String location;
    private String category;
    private String imageUri;
    private String timestamp;
    private double latitude;
    private double longitude;

    public LostFoundItem() {
    }

    public LostFoundItem(String postType, String name, String phone, String description,
                         String dateText, String location, String category,
                         String imageUri, String timestamp, double latitude, double longitude) {
        this.postType = postType;
        this.name = name;
        this.phone = phone;
        this.description = description;
        this.dateText = dateText;
        this.location = location;
        this.category = category;
        this.imageUri = imageUri;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPostType() {
        return postType;
    }

    public void setPostType(String postType) {
        this.postType = postType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDateText() {
        return dateText;
    }

    public void setDateText(String dateText) {
        this.dateText = dateText;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean hasValidCoordinates() {
        return !(latitude == 0.0 && longitude == 0.0);
    }
}
