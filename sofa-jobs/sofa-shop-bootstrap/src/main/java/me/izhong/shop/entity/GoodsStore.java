package me.izhong.shop.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.*;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "PRODUCT_STORE")
@SequenceGenerator(name = "PRODUCT_STORE_SEQ", sequenceName = "PRODUCT_STORE_SEQ", allocationSize = 1)
public class GoodsStore extends EditableEntity{
    @Id
    @GeneratedValue(generator = "PRODUCT_STORE_SEQ", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "PRODUCT_ID")
    private Long productId;
    @Column(name = "ATTR_ID")
    private Long productAttrId;
    @Version
    private Integer version;
    @Column(name = "STORE")
    private Integer store;
    @Column(name = "PRE_STORE")
    private Integer preStore;
}
