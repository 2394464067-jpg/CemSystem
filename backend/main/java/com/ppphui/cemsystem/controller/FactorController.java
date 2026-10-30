package com.ppphui.cemsystem.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ppphui.cemsystem.common.Result;
import com.ppphui.cemsystem.dto.FactorSaveDTO;
import com.ppphui.cemsystem.dto.FactorVO;
import com.ppphui.cemsystem.service.FactorService;
import com.ppphui.cemsystem.dto.FactorVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "因子管理相关", description = "分页查询，根据 id 查询，根据名称查询，新增，修改，删除")
@RestController
@RequestMapping("/api/factor")
@RequiredArgsConstructor
public class FactorController {

    private final FactorService factorService;

    @Operation(summary = "分页查询", description = "分页查询")
    @GetMapping("/page")
    public Result<IPage<FactorVO>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String energyName) {
        return Result.success(factorService.page(page, size, energyName));
    }

    @Operation(summary = "根据 id 查询", description = "根据 id 查询")
    @GetMapping("/{id}")
    public Result<FactorVO> getById(@PathVariable Long id) {
        return Result.success(factorService.getById(id));
    }

    @Operation(summary = "根据名称查询", description = "根据名称查询")
    @GetMapping("/name")
    public Result<FactorVO> getByEnergyName(@RequestParam String energyName) {
        return Result.success(factorService.getByEnergyName(energyName));
    }

    @Operation(summary = "新增", description = "新增")
    @PostMapping
    public Result<Void> save(@RequestBody @Valid FactorSaveDTO dto) {
        factorService.save(dto);
        return Result.success();
    }
    @Operation(summary = "修改", description = "修改")
    @PutMapping
    public Result<Void> update(@RequestBody @Valid FactorSaveDTO dto) {
        factorService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除", description = "删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        factorService.delete(id);
        return Result.success();
    }
}
