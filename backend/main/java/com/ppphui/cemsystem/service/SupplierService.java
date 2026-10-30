package com.ppphui.cemsystem.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ppphui.cemsystem.dto.SupplierBindDTO;
import com.ppphui.cemsystem.dto.SupplierDetailVO;
import com.ppphui.cemsystem.dto.SupplierQueryDTO;
import com.ppphui.cemsystem.dto.SupplierSaveDTO;
import com.ppphui.cemsystem.dto.SupplierVO;

public interface SupplierService {

    IPage<SupplierVO> page(SupplierQueryDTO query);

    SupplierDetailVO getDetailById(Integer id);

    void save(SupplierSaveDTO dto);
    void update(SupplierSaveDTO dto);
    void delete(Integer id);

    void bindRecords(SupplierBindDTO dto);
    void unbindRecord(Integer recordId);


    void recalculateGreenLevels();
}
