package com.muke.dao;


import com.muke.entity.EcommerceUser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * <h1>EcommerceUser Dao 接口定义</h1>
 * */
public interface EcommerceUserDao extends JpaRepository<EcommerceUser, Long> {

    /**
     * <h2>根据用户名查询 EcommerceUser 对象</h2>
     * select * from t_ecommerce_user where username = ?
     * */
    EcommerceUser findByUsername(String username);

    /**
     * <h2>根据用户名和密码查询实体对象</h2>
     * select * from t_ecommerce_user where username = ? and password = ?
     * */
    EcommerceUser findByUsernameAndPassword(String username, String password);
}
