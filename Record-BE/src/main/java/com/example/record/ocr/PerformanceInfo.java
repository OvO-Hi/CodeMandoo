package com.example.record.ocr;

public class PerformanceInfo {
    private String title;
    private String date;
    private String time;
    private String venue;
    private String artist;

    public PerformanceInfo() {}
    public PerformanceInfo(String title, String date, String time, String venue, String artist) {
        this.title = title; this.date = date; this.time = time; this.venue = venue; this.artist = artist;
    }
    public String getTitle() { return title; }  public void setTitle(String title) { this.title = title; }
    public String getDate() { return date; }    public void setDate(String date) { this.date = date; }
    public String getTime() { return time; }    public void setTime(String time) { this.time = time; }
    public String getVenue() { return venue; }  public void setVenue(String venue) { this.venue = venue; }
    public String getArtist() { return artist; }public void setArtist(String artist) { this.artist = artist; }
}
