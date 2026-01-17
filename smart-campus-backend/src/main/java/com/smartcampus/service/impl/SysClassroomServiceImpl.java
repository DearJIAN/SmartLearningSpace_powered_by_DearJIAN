package com.smartcampus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcampus.entity.SysClassroom;
import com.smartcampus.mapper.SysClassroomMapper;
import com.smartcampus.service.SysClassroomService;
import org.springframework.stereotype.Service;

/**
 * 教室Service实现类
 */
@Service
public class SysClassroomServiceImpl extends ServiceImpl<SysClassroomMapper, SysClassroom> implements SysClassroomService {
}
