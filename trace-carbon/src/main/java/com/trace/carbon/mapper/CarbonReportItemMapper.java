package com.trace.carbon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trace.carbon.entity.CarbonReportItem;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 碳足迹分项明细 Mapper
 */
public interface CarbonReportItemMapper extends BaseMapper<CarbonReportItem> {

    @Select("SELECT * FROM carbon_report_item WHERE report_id = #{reportId} AND deleted = 0")
    List<CarbonReportItem> selectByReportId(@Param("reportId") Long reportId);
}
