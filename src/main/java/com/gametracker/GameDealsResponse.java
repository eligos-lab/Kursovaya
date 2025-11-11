package com.gametracker;

import java.util.List;

public class GameDealsResponse {
    private GameInfo info;
    private List<Deal> deals;
    private List<CheapestPrice> cheapestPrice;

    // Getters and Setters
    public GameInfo getInfo() { return info; }
    public void setInfo(GameInfo info) { this.info = info; }

    public List<Deal> getDeals() { return deals; }
    public void setDeals(List<Deal> deals) { this.deals = deals; }

    public List<CheapestPrice> getCheapestPrice() { return cheapestPrice; }
    public void setCheapestPrice(List<CheapestPrice> cheapestPrice) { this.cheapestPrice = cheapestPrice; }

    // Вложенные классы
    public static class GameInfo {
        private String title;
        private String steamAppID;
        private String thumb;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getSteamAppID() { return steamAppID; }
        public void setSteamAppID(String steamAppID) { this.steamAppID = steamAppID; }

        public String getThumb() { return thumb; }
        public void setThumb(String thumb) { this.thumb = thumb; }
    }

    public static class CheapestPrice {
        private String price;
        private String date;

        public String getPrice() { return price; }
        public void setPrice(String price) { this.price = price; }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
    }
}