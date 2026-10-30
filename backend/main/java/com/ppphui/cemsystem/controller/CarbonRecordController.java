package com.ppphui.cemsystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ppphui.cemsystem.common.Result;
import com.ppphui.cemsystem.dto.*;
import com.ppphui.cemsystem.log.Log;
import com.ppphui.cemsystem.service.CarbonRecordService;
import com.ppphui.cemsystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "碳排记录上报相关", description = "分页查询，根据 id 查询详情，保存草稿，提交审核，审核通过，驳回，重新提交，删除（仅草稿/驳回可删），近 N 月排放趋势（大屏折线图），可绑定记录（审核通过且未绑定供应商）")
@RestController
@RequestMapping("/api/carbon-record")
@RequiredArgsConstructor
public class CarbonRecordController {

    private final CarbonRecordService carbonRecordService;
    private final UserService userService; // 用于获取当前登录用户

    @Operation(summary = "分页查询碳排记录")
    @GetMapping("/page")
    public Result<IPage<CarbonRecordVO>> page(CarbonRecordQueryDTO query) {
        Integer currentUserId = userService.getCurrentUserId();
        boolean isAdmin = userService.isAdmin();
        return Result.success(carbonRecordService.page(query, currentUserId, isAdmin));
    }

    @Operation(summary = "根据 id 查询详情")
    @GetMapping("/{id}")
    public Result<CarbonRecordVO> getById(@PathVariable Integer id) {
        return Result.success(carbonRecordService.getDetailById(id));
    }

    @Operation(summary = "保存草稿")
    @PostMapping("/draft")
    public Result<Void> saveDraft(@RequestBody @Valid CarbonRecordSaveDTO dto) {
        carbonRecordService.saveDraft(dto, userService.getCurrentUserId());
        return Result.success();
    }

    @Operation(summary = "保存并提交")
    @PostMapping("/saveAndSubmit")
    public Result<Void> saveAndSubmit(@RequestBody @Valid CarbonRecordSaveAndSubmitDTO dto) {
        carbonRecordService.saveAndSubmit(dto);
        return Result.success();
    }

    @Log(module = "碳排记录", type = "提交审核")
    @Operation(summary = "提交审核")
    @PostMapping("/submit/{id}")
    public Result<Void> submit(@PathVariable int id) {
        carbonRecordService.submit(id);
        return Result.success();
    }

    @Log(module = "碳排记录", type = "审核通过")
    @Operation(summary = "审核通过")
    @PostMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable int id) {
        carbonRecordService.approve(id);
        return Result.success();
    }

    @Log(module = "碳排记录", type = "驳回")
    @Operation(summary = "驳回")
    @PostMapping("/reject/{id}")
    public Result<Void> reject(@PathVariable int id) {
        carbonRecordService.reject(id);
        return Result.success();
    }
    @Operation(summary = "重新提交")
    @PostMapping("/resubmit/{id}")
    public Result<Void> reSubmit(@PathVariable int id) {
        carbonRecordService.reSubmit(id);
        return Result.success();
    }
    @Log(module = "碳排记录", type = "删除")
    @Operation(summary = "删除（仅草稿/驳回可删）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        carbonRecordService.delete(id, userService.getCurrentUserId(), userService.isAdmin());
        return Result.success();
    }

    @Operation(summary = "近 N 月排放趋势（大屏折线图）")
    @GetMapping("/trend")
    public Result<List<CarbonRecordStatVO>> trend(@RequestParam(defaultValue = "12") int months) {
        return Result.success(carbonRecordService.monthlyTrend(months));
    }
    @Operation(summary = "可绑定记录（审核通过且未绑定供应商）")
    @GetMapping("/bindable")
    public Result<List<CarbonRecordVO>> bindable(@RequestParam(required = false) String reportMonth) {
        return Result.success(carbonRecordService.listBindable(reportMonth));
    }
}