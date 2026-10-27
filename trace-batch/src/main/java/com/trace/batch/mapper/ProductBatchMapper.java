package com.trace.batch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trace.batch.entity.ProductBatch;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 农产品批次 Mapper
 */
public interface ProductBatchMapper extends BaseMapper<ProductBatch> {

    @Select("SELECT * FROM product_batch WHERE batch_no = #{batchNo} AND deleted = 0 LIMIT 1")
    ProductBatch selectByBatchNo(@Param("batchNo") String batchNo);
}
