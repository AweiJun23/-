package com.trace.carbon.controller;

import com.trace.carbon.service.CarbonService;
import com.trace.carbon.vo.CarbonReportVO;
import com.trace.common.result.R;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 碳足迹核算接口
 */
@RestController
@RequestMapping("/api/carbon")
public class CarbonController {

    @Resource
    private CarbonService carbonService;

    @GetMapping("/report/{batchId}")
    public R<CarbonReportVO> report(@PathVariable("batchId") Long batchId) {
        return R.ok(carbonService.getReport(batchId));
    }

    @GetMapping("/report/no/{batchNo}")
    public R<CarbonReportVO> reportByNo(@PathVariable("batchNo") String batchNo) {
        return R.ok(carbonService.getReportByBatchNo(batchNo));
    }

    @GetMapping("/internal/report/{batchId}")
    public R<CarbonReportVO> internalReport(@PathVariable("batchId") Long batchId) {
        return R.ok(carbonService.getReport(batchId));
    }
}
