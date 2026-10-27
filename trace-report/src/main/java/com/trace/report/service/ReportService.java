package com.trace.report.service;

import com.trace.report.vo.SubsidyReportVO;

/**
 * 补贴报表业务接口
 */
public interface ReportService {

    /** 生成标准化补贴申报材料 */
    Long generate(Long batchId, Long farmerId);

    /** 查询批次最新申报材料 */
    SubsidyReportVO latest(Long batchId);
}
