package com.smartcampus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcampus.entity.SysUser;
import com.smartcampus.mapper.SysUserMapper;
import com.smartcampus.service.SysUserService;
import org.springframework.stereotype.Service;

/**
 * 用户Service实现类
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
}
