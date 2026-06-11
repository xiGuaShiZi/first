package com.example.enterprise.common;

/**
 * 订单状态常量
 * <p>集中管理订单、商品审核、结算等状态码，避免字符串硬编码散落在各处</p>
 */
public final class OrderStatus {

    private OrderStatus() {}

    // ========== 订单状态 ==========
    /** 待付款 */
    public static final String WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
    /** 待发货 */
    public static final String WAIT_SELLER_SEND_GOODS = "WAIT_SELLER_SEND_GOODS";
    /** 待确认收货 */
    public static final String WAIT_BUYER_CONFIRM_GOODS = "WAIT_BUYER_CONFIRM_GOODS";
    /** 交易完成 */
    public static final String TRADE_FINISHED = "TRADE_FINISHED";
    /** 交易关闭 */
    public static final String TRADE_CLOSED = "TRADE_CLOSED";
    /** 退款中 */
    public static final String REFUNDING = "REFUNDING";
    /** 退货中 */
    public static final String RETURNING = "RETURNING";
    /** 售后完成 */
    public static final String AFTER_SALES_FINISHED = "AFTER_SALES_FINISHED";
    /** 已结算 */
    public static final String SETTLED = "SETTLED";

    // ========== 审核状态 ==========
    /** 待审核 */
    public static final int AUDIT_PENDING = 0;
    /** 审核通过 */
    public static final int AUDIT_APPROVED = 1;
    /** 审核拒绝 */
    public static final int AUDIT_REJECTED = 2;

    // ========== 商品状态 ==========
    /** 下架 */
    public static final int PRODUCT_OFFLINE = 0;
    /** 上架 */
    public static final int PRODUCT_ONLINE = 1;

    // ========== 结算状态 ==========
    /** 未结算 */
    public static final String SETTLEMENT_UNSETTLED = "UNSETTLED";
    /** 已结算 */
    public static final String SETTLEMENT_SETTLED = "SETTLED";
    /** 已退款 */
    public static final String SETTLEMENT_REFUNDED = "REFUNDED";

    // ========== 议价状态 ==========
    /** 待处理 */
    public static final String BARGAIN_PENDING = "PENDING";
    /** 已接受 */
    public static final String BARGAIN_ACCEPTED = "ACCEPTED";
    /** 已拒绝 */
    public static final String BARGAIN_REJECTED = "REJECTED";
    /** 已取消 */
    public static final String BARGAIN_CANCELLED = "CANCELLED";
    /** 已使用 */
    public static final String BARGAIN_USED = "USED";

    // ========== 交易协商状态 ==========
    /** 申请待处理 */
    public static final String RETURN_APPLY_PENDING = "APPLY_PENDING";

    // ========== 评价类型 ==========
    /** 主评价 */
    public static final String REVIEW_MAIN = "MAIN";
    /** 追加评价 */
    public static final String REVIEW_ADDITIONAL = "ADDITIONAL";

    // ========== 交易协商类型 ==========
    /** 仅退款 */
    public static final String RETURN_ONLY_REFUND = "ONLY_REFUND";
    /** 退货退款 */
    public static final String RETURN_REFUND = "RETURN_REFUND";
    /** 换货 */
    public static final String RETURN_EXCHANGE = "EXCHANGE";
    /** 维修 */
    public static final String RETURN_REPAIR = "REPAIR";
}
