package com.muke.dao;

import com.muke.entity.EcommerceLogistics;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * <h1>EcommerceLogistics Dao 接口定义</h1>
 * */
public interface EcommerceLogisticsDao extends JpaRepository<EcommerceLogistics, Long> {
}
