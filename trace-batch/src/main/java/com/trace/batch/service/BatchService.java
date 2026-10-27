package com.trace.batch.service;

import com.trace.batch.entity.ProductBatch;
import com.trace.batch.service.dto.BatchTraceVO;
import com.trace.batch.service.dto.CreateBatchRequest;
import java.util.List;

/**
 * 农产品批次业务接口
 */
public interface BatchService {

    Long createBatch(CreateBatchRequest request);

    ProductBatch getById(Long id);

    ProductBatch getByBatchNo(String batchNo);

    /** 提供给其它服务的内部查询 */
    ProductBatch internalGet(Long id);

    /** 生成溯源二维码 PNG 字节 */
    byte[] qrImage(String batchNo);

    /** 生成溯源二维码 Base64 data URL（JSON 传输，规避二进制经代理的兼容问题） */
    String qrBase64(String batchNo);

    /** 聚合溯源 + 碳足迹 + 链上验真（H5 扫码消费） */
    BatchTraceVO traceAggregate(String batchNo);

    /** 按合作社列出批次（收购商后台） */
    List<ProductBatch> listByCoop(Long coopId);
}
