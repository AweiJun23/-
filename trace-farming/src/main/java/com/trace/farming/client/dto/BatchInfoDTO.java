package com.trace.farming.client.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

/**
 * 批次内部传输对象（跨服务调用使用，避免直接依赖对方实体）
 */
@Data
public class BatchInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String batchNo;
    private Long farmerId;
    private String productName;
    private String originAddress;
    private LocalDate plantStartDate;
    private LocalDate plantEndDate;
    private BigDecimal areaMu;
    private BigDecimal expectedYield;
    private String status;
    private String qrCode;
}
