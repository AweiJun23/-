package com.trace.farming.client.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 批次精简视图，供农事服务做归属校验
 */
@Data
public class BatchLiteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String batchNo;
    private Long farmerId;
    private Long coopId;
    private String productName;
    private String origin;
    private String status;
}
