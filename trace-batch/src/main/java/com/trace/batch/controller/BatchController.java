package com.trace.batch.controller;

import com.trace.batch.entity.ProductBatch;
import com.trace.batch.service.BatchService;
import com.trace.batch.service.dto.BatchTraceVO;
import com.trace.batch.service.dto.CreateBatchRequest;
import com.trace.common.context.UserContext;
import com.trace.common.result.R;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 农产品批次接口
 */
@RestController
@RequestMapping("/api/batch")
public class BatchController {

    @Resource
    private BatchService batchService;

    @PostMapping("/create")
    public R<Long> create(@Valid @RequestBody CreateBatchRequest request) {
        return R.ok(batchService.createBatch(request));
    }

    @GetMapping("/{id}")
    public R<ProductBatch> detail(@PathVariable("id") Long id) {
        return R.ok(batchService.getById(id));
    }

    @GetMapping("/no/{batchNo}")
    public R<ProductBatch> byNo(@PathVariable("batchNo") String batchNo) {
        return R.ok(batchService.getByBatchNo(batchNo));
    }

    @GetMapping("/qr/{batchNo}")
    public void qr(@PathVariable("batchNo") String batchNo, HttpServletResponse response) throws Exception {
        response.setContentType("image/png");
        response.getOutputStream().write(batchService.qrImage(batchNo));
        response.getOutputStream().flush();
    }

    /** 二维码 Base64 data URL（JSON，前端 <img> 直接用，规避二进制代理兼容问题） */
    @GetMapping("/qr-base64/{batchNo}")
    public R<String> qrBase64(@PathVariable("batchNo") String batchNo) {
        return R.ok(batchService.qrBase64(batchNo));
    }

    @GetMapping("/trace/{batchNo}")
    public R<BatchTraceVO> trace(@PathVariable("batchNo") String batchNo) {
        return R.ok(batchService.traceAggregate(batchNo));
    }

    @GetMapping("/internal/{id}")
    public R<ProductBatch> internal(@PathVariable("id") Long id) {
        return R.ok(batchService.internalGet(id));
    }

    /** 收购商后台：列出本合作社批次 */
    @GetMapping("/list")
    public R<List<ProductBatch>> list() {
        Long coopId = UserContext.get() == null ? null : UserContext.get().getCoopId();
        return R.ok(batchService.listByCoop(coopId));
    }
}
