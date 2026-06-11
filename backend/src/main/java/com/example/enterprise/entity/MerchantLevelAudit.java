package com.example.enterprise.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "merchant_level_audit")
public class MerchantLevelAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "operator_name", length = 128)
    private String operatorName;

    @Column(name = "old_level")
    private Integer oldLevel;

    @Column(name = "new_level")
    private Integer newLevel;

    @Column(length = 255)
    private String reason;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    public MerchantLevelAudit() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public Integer getOldLevel() { return oldLevel; }
    public void setOldLevel(Integer oldLevel) { this.oldLevel = oldLevel; }
    public Integer getNewLevel() { return newLevel; }
    public void setNewLevel(Integer newLevel) { this.newLevel = newLevel; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}

