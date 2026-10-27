package com.trace.batch.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建农产品批次请求
 */
@Data
public class CreateBatchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "农户ID不能为空")
    private Long farmerId;

    private Long coopId;

    @NotBlank(message = "产品名称不能为空")
    private String productName;

    private String originProvince;
    private String originCity;
    private String originDetail;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate plantStartDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate plantEndDate;

    private BigDecimal areaMu;
    private BigDecimal expectedYield;
    private BigDecimal actualYield;

    private String status;
}
