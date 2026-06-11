package com.example.enterprise.repository;

import com.example.enterprise.entity.ServiceFee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 平台服务费数据访问接口
 */
public interface ServiceFeeRepository extends JpaRepository<ServiceFee, Long> {
    /** 根据商家ID查询服务费记录 */
    List<ServiceFee> findByMerchantIdOrderByCreateTimeDesc(Long merchantId);

    /** 根据订单ID查询服务费记录 */
    ServiceFee findByOrderId(Long orderId);
}
