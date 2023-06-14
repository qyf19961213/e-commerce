package com.muke.service.impl;

import com.muke.account.BalanceInfo;
import com.muke.dao.EcommerceBalanceDao;
import com.muke.entity.EcommerceBalance;
import com.muke.filter.AccessContext;
import com.muke.service.IBalanceService;
import com.muke.vo.LoginUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <h1>用于余额相关服务接口实现</h1>
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class BalanceServiceImpl implements IBalanceService {

    private final EcommerceBalanceDao balanceDao;

    public BalanceServiceImpl(EcommerceBalanceDao balanceDao) {
        this.balanceDao = balanceDao;
    }


    @Override
    public BalanceInfo getCurrentUserBalanceInfo() {
        LoginUserInfo loginUserInfo = AccessContext.getLoginUserInfo();
        BalanceInfo balanceInfo = new BalanceInfo(
                loginUserInfo.getId(), 0L
        );

        EcommerceBalance ecommerceBalance =
                balanceDao.findByUserId(loginUserInfo.getId());
        if (null != ecommerceBalance) {
            balanceInfo.setBalance(ecommerceBalance.getBalance());
        } else {
            // 如果还没有用户余额记录，这里创建出来，余额设定为0
            EcommerceBalance newBalance = new EcommerceBalance();
            newBalance.setUserId(loginUserInfo.getId());
            newBalance.setBalance(0L);
            log.info("init user balance record: [{}]",
                    balanceDao.save(newBalance).getId());
        }
        return balanceInfo;
    }

    @Override
    public BalanceInfo deductBalance(BalanceInfo balanceInfo) {
        LoginUserInfo loginUserInfo = AccessContext.getLoginUserInfo();

        EcommerceBalance ecommerceBalance =
                balanceDao.findByUserId(loginUserInfo.getId());
        if (null == ecommerceBalance
                || ecommerceBalance.getBalance() - balanceInfo.getBalance() < 0
        ) {
            throw new RuntimeException("user balance is not enough!");
        }

        Long sourceBalance = ecommerceBalance.getBalance();
        ecommerceBalance.setBalance(ecommerceBalance.getBalance() - balanceInfo.getBalance());
        log.info("deduct balance: [{}], [{}], [{}]",
                balanceDao.save(ecommerceBalance).getId(), sourceBalance,
                balanceInfo.getBalance());
        return new BalanceInfo(
                ecommerceBalance.getUserId(),
                ecommerceBalance.getBalance()
        );
    }
}
