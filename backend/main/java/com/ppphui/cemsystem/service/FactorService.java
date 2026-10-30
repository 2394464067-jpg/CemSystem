package com.ppphui.cemsystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ppphui.cemsystem.dto.FactorSaveDTO;
import com.ppphui.cemsystem.dto.FactorSaveDTO;
import com.ppphui.cemsystem.dto.FactorVO;
import com.ppphui.cemsystem.pojo.Factor;

public interface FactorService {

    /** 分页查询 */
    IPage<FactorVO> page(Integer page, Integer size, String energyName);
    /** 新增 */
    void save(FactorSaveDTO dto);

    /** 修改 */
    void update(FactorSaveDTO dto);

    /** 删除 */
    void delete(Long id);

    /** 根据 id 查询 */
    FactorVO getById(Long id);


    /** 根据因子名称（能源类型）查询 */
    FactorVO getByEnergyName(String energyName);

}
