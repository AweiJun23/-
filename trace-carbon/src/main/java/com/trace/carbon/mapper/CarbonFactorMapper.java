package com.trace.carbon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trace.carbon.entity.CarbonFactor;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 碳排放因子 Mapper
 */
public interface CarbonFactorMapper extends BaseMapper<CarbonFactor> {

    @Select("SELECT * FROM carbon_factor WHERE farming_type = #{type} AND material_name = #{material} AND deleted = 0 LIMIT 1")
    CarbonFactor selectByTypeAndMaterial(@Param("type") String type, @Param("material") String material);

    @Select("SELECT * FROM carbon_factor WHERE farming_type = #{type} AND deleted = 0")
    List<CarbonFactor> selectByType(@Param("type") String type);
}
