package com.helios.cctv.mapper;

import com.helios.cctv.CctvApplication;
import com.helios.cctv.dto.cctv.CctvDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CctvMapper {
    void insert(@Param("cctv") CctvDTO cctv);
}
