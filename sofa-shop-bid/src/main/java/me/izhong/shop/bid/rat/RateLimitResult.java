package me.izhong.shop.bid.rat;

public enum RateLimitResult {

    SUCCESS(1L),
    NO_LIMIT(0L),
    ACQUIRE_FAIL(-1L),
    MODIFY_ERROR(-2L),
    UNSUPPORT_METHOD(-500L),
    ERROR(-505L);

    private Long code;

    RateLimitResult(Long code){
        this.code = code;
    }

    public static RateLimitResult getResult(Long code){
        for(RateLimitResult enums: RateLimitResult.values()){
            if(enums.code.equals(code)){
                return enums;
            }
        }
        throw new IllegalArgumentException("unknown rate limit return code:" + code);
    }
}