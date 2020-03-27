package me.izhong.shop.bid.rat;

public enum RateLimitMethod {

    //initialize rate limiter
    init,

    //modify rate limiter parameter
    modify,

    //delete rate limiter
    delete,

    //acquire permits
    acquire;
}