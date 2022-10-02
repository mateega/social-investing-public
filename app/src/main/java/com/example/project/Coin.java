package com.example.project;

public class Coin {

    private String name;
    private String ticker;
    private String price;
    private String priceChange;
    private String rank;
    private String imageUrl;

    public String getName() {
        return name;
    }

    public String getTicker() {
        return ticker;
    }

    public String getPrice() {
        return price;
    }

    public String getPriceChange() {
        return priceChange;
    }

    public String getRank() {
        return rank;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setPriceChange(String priceChange) {
        this.priceChange = priceChange;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}


