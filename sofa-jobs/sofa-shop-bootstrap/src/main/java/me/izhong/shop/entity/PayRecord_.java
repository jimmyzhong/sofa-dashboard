package me.izhong.shop.entity;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@StaticMetamodel(PayRecord.class)
public class PayRecord_ extends PersistedEntity{
    public static volatile SingularAttribute<PayRecord, Long> id;
    public static volatile SingularAttribute<PayRecord, BigDecimal> totalAmount;
    public static volatile SingularAttribute<PayRecord, BigDecimal> payAmount;
    public static volatile SingularAttribute<PayRecord, String> internalId;
    public static volatile SingularAttribute<PayRecord, String> externalId;
    public static volatile SingularAttribute<PayRecord, String> payMethod;
    public static volatile SingularAttribute<PayRecord, String> type;
    public static volatile SingularAttribute<PayRecord, String> state;
    public static volatile SingularAttribute<PayRecord, String> comment;
    public static volatile SingularAttribute<PayRecord, Long> payerId;
    public static volatile SingularAttribute<PayRecord, Long> receiverId;
    public static volatile SingularAttribute<PayRecord, LocalDateTime> createTime;
}
