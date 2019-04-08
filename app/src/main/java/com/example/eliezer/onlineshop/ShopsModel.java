package com.example.eliezer.onlineshop;

/**
 * Created by Eliezer on 30/03/2018.
 */

public class ShopsModel {
    private String Shop_Name;
    private String Shop_Description;
    private String Shop_Logo;
    private String Shop_Address;
    private String Shop_Longitude;
    private String Shop_Latitude;
    private String Shop_User;
    private String status;

    public ShopsModel() {
    }

    public ShopsModel(String shop_Name, String shop_Description, String shop_Logo, String shop_Address, String shop_Longitude, String shop_Latitude, String shop_User, String status) {
        Shop_Name = shop_Name;
        Shop_Description = shop_Description;
        Shop_Logo = shop_Logo;
        Shop_Address = shop_Address;
        Shop_Longitude = shop_Longitude;
        Shop_Latitude = shop_Latitude;
        Shop_User = shop_User;
        status = status;
    }

    public String getShop_Name() {
        return Shop_Name;
    }

    public void setShop_Name(String shop_Name) {
        Shop_Name = shop_Name;
    }

    public String getShop_Description() {
        return Shop_Description;
    }

    public void setShop_Description(String shop_Description) {
        Shop_Description = shop_Description;
    }

    public String getShop_Logo() {
        return Shop_Logo;
    }

    public void setShop_Logo(String shop_Logo) {
        Shop_Logo = shop_Logo;
    }

    public String getShop_Address() {
        return Shop_Address;
    }

    public void setShop_Address(String shop_Address) {
        Shop_Address = shop_Address;
    }

    public String getShop_Longitude() {
        return Shop_Longitude;
    }

    public void setShop_Longitude(String shop_Longitude) {
        Shop_Longitude = shop_Longitude;
    }

    public String getShop_Latitude() {
        return Shop_Latitude;
    }

    public void setShop_Latitude(String shop_Latitude) {
        Shop_Latitude = shop_Latitude;
    }

    public String getShop_User() {
        return Shop_User;
    }

    public void setShop_User(String shop_User) {
        Shop_User = shop_User;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
