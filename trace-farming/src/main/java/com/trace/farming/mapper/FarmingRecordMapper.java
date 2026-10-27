package com.trace.farming.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trace.farming.entity.FarmingRecord;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 农事记录 Mapper
 */
public interface FarmingRecordMapper extends BaseMapper<FarmingRecord> {

    /**
     * 查询某批次下全部农事记录（按作业时间升序，用于溯源时间线展示）
     */
    @Select("SELECT * FROM farming_record WHERE batch_id = #{batchId} AND deleted = 0 ORDER BY work_date ASC")
    List<FarmingRecord> selectByBatchId(@Param("batchId") Long batchId);

    /**
     * 幂等去重：按同步令牌查询已入库记录数
     */
    @Select("SELECT COUNT(1) FROM farming_record WHERE sync_token = #{token} AND deleted = 0")
    Long countBySyncToken(@Param("token") String token);
}
