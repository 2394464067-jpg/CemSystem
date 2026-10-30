package com.ppphui.cemsystem.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ppphui.cemsystem.common.Result;
import com.ppphui.cemsystem.dto.SupplierBindDTO;
import com.ppphui.cemsystem.dto.SupplierDetailVO;
import com.ppphui.cemsystem.dto.SupplierQueryDTO;
import com.ppphui.cemsystem.dto.SupplierSaveDTO;
import com.ppphui.cemsystem.dto.SupplierVO;
import com.ppphui.cemsystem.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "供应商管理相关", description = "分页查询供应商、根据 id 查询供应商详情、新增供应商、修改供应商、删除供应商、将碳排记录绑定到供应商、解绑碳排记录、手动触发绿色等级计算")
@RestController
@RequestMapping("/api/supplier")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @Operation(summary = "分页查询供应商", description = "分页查询供应商列表")
    @GetMapping("/page")
    public Result<IPage<SupplierVO>> page(SupplierQueryDTO query) {
        return Result.success(supplierService.page(query));
    }

    @Operation(summary = "根据 id 查询供应商详情", description = "根据 id 查询供应商详情（含最近绑定的碳排记录）")
    @GetMapping("/{id}")
    public Result<SupplierDetailVO> getById(@PathVariable Integer id) {
        return Result.success(supplierService.getDetailById(id));
    }
    @Operation(summary = "新增供应商", description = "新增供应商")
    @PostMapping
    public Result<Void> save(@RequestBody @Valid SupplierSaveDTO dto) {
        supplierService.save(dto);
        return Result.success();
    }

    @Operation(summary = "修改供应商", description = "修改供应商")
    @PutMapping
    public Result<Void> update(@RequestBody @Valid SupplierSaveDTO dto) {
        supplierService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除供应商", description = "删除供应商（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        supplierService.delete(id);
        return Result.success();
    }

    @Operation(summary = "将碳排记录绑定到供应商", description = "将碳排记录绑定到供应商（支持批量）")
    @PostMapping("/bind")
    public Result<Void> bindRecords(@RequestBody @Valid SupplierBindDTO dto) {
        supplierService.bindRecords(dto);
        return Result.success();
    }

    @Operation(summary = "解绑碳排记录", description = "将某条碳排记录从供应商上移除")
    @DeleteMapping("/unbind/{recordId}")
    public Result<Void> unbindRecord(@PathVariable Integer recordId) {
        supplierService.unbindRecord(recordId);
        return Result.success();
    }

    @Operation(summary = "手动触发绿色等级计算", description = "手动触发绿色等级计算（仅管理员，便于测试/补算）")
    @PostMapping("/recalculate-level")
    public Result<Void> recalculateLevel() {
        supplierService.recalculateGreenLevels();
        return Result.success();
    }
}