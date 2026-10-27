package com.trace.report.controller;

import com.trace.report.service.ReportService;
import com.trace.report.vo.SubsidyReportVO;
import com.trace.common.result.R;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 补贴报表接口
 */
@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Resource
    private ReportService reportService;

    @PostMapping("/subsidy/generate")
    public R<Long> generate(@RequestParam("batchId") Long batchId,
                            @RequestParam(value = "farmerId", required = false) Long farmerId) {
        return R.ok(reportService.generate(batchId, farmerId));
    }

    @GetMapping("/subsidy/latest/{batchId}")
    public R<SubsidyReportVO> latest(@PathVariable("batchId") Long batchId) {
        return R.ok(reportService.latest(batchId));
    }

    /** 下载 Markdown 申报材料 */
    @GetMapping("/subsidy/download/{batchId}")
    public void download(@PathVariable("batchId") Long batchId, HttpServletResponse response) throws Exception {
        SubsidyReportVO vo = reportService.latest(batchId);
        response.setContentType("text/markdown;charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment;filename=" + java.net.URLEncoder.encode(vo.getReportNo() + ".md", "UTF-8"));
        response.getWriter().write(vo.getContentMd());
        response.getWriter().flush();
    }
}
