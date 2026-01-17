package com.smartcampus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcampus.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
