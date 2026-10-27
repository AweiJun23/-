package com.trace.carbon.service;

import com.trace.carbon.vo.CarbonReportVO;
import com.trace.common.domain.event.FarmingRecordEvent;

/**
 * 碳足迹核算业务接口
 */
public interface CarbonService {

    /** 消费农事事件，计算并落库碳足迹报告 */
    void computeOnFarmingEvent(FarmingRecordEvent event);

    /** 内部 / 聚合查询：按批次 */
    CarbonReportVO getReport(Long batchId);

    /** 公开查询：按批次编号 */
    CarbonReportVO getReportByBatchNo(String batchNo);
}
