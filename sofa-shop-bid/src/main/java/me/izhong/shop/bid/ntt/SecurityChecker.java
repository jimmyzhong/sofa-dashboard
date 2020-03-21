package me.izhong.shop.bid.ntt;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.bid.config.ConfigBean;
import me.izhong.shop.bid.frame.BidContext;
import me.izhong.shop.bid.frame.ISecurityChecker;



@Slf4j
@Getter
public class SecurityChecker implements ISecurityChecker {

    protected ConfigBean configBean;

    public SecurityChecker( ConfigBean configBean ) {
         this.configBean = configBean;
    }

    public void checkSign(BidContext context) throws BusinessException {

    }

}
