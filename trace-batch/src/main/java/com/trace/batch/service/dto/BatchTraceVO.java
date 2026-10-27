package com.trace.batch.service.dto;

import com.trace.batch.client.dto.CarbonReportLiteDTO;
import com.trace.batch.client.dto.FarmingLiteDTO;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

/**
 * 溯源聚合视图（H5 消费者扫码查看）
 */
@Data
public class BatchTraceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String batchNo;
    private String productName;
    private String originProvince;
    private String originCity;
    private String originDetail;
    private LocalDate plantStartDate;
    private LocalDate plantEndDate;
    private BigDecimal areaMu;
    private BigDecimal actualYield;
    private String status;
    private String qrUrl;
    private String flowInfo;

    /** 农事时间线 */
    private List<FarmingLiteDTO> farmingList;

    /** 碳足迹报告 */
    private CarbonReportLiteDTO carbonReport;

    /** 批次数据是否链上验真通过 */
    private Boolean batchVerified;

    /** 碳报告是否链上验真通过 */
    private Boolean carbonVerified;
}
