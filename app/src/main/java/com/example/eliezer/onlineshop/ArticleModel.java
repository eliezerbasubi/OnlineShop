package com.example.eliezer.onlineshop;

/**
 * Created by verdotte on 3/23/2018.
 */

public class ArticleModel {

    String price_list;
    String product_list;
    String image_list;
    String shop_user;
    String description_list;

    public ArticleModel() {
    }

    public ArticleModel(String price_list, String product_list, String image_list, String shop_user, String description_list) {
        this.price_list = price_list;
        this.product_list = product_list;
        this.image_list = image_list;
        this.shop_user = shop_user;
        this.description_list = description_list;
    }

    public String getPrice_list() {
        return price_list;
    }

    public void setPrice_list(String price_list) {
        this.price_list = price_list;
    }

    public String getProduct_list() {
        return product_list;
    }

    public void setProduct_list(String product_list) {
        this.product_list = product_list;
    }

    public String getImage_list() {
        return image_list;
    }

    public void setImage_list(String image_list) {
        this.image_list = image_list;
    }

    public String getShop_user() {
        return shop_user;
    }

    public void setShop_user(String shop_user) {
        this.shop_user = shop_user;
    }

    public String getDescription_list() {
        return description_list;
    }

    public void setDescription_list(String description_list) {
        this.description_list = description_list;
    }
}
