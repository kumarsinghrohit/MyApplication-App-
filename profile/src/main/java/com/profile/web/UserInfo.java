package com.profile.web;

/**
 * The value Object used for exchanging instances with the client side.
 */

class UserInfo {

    private final String email;
    private final String imageUrl;

    UserInfo(String email, String imageUrl) {
        this.email = email;
        this.imageUrl = imageUrl;
    }

    public String getEmail() {
        return email;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
