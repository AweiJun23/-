package com.trace.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trace.report.entity.SubsidyReport;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 补贴申报报表 Mapper
 */
public interface SubsidyReportMapper extends BaseMapper<SubsidyReport> {

    @Select("SELECT * FROM subsidy_report WHERE batch_id = #{batchId} AND deleted = 0 ORDER BY generated_time DESC LIMIT 1")
    SubsidyReport selectLatestByBatchId(@Param("batchId") Long batchId);
}
