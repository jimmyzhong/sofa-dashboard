package me.izhong.jobs.model.bid;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public class BidItems implements Serializable {

    private Long allow;

    private Long seqId;

    private Long price;

}
