package com.smartcampus.controller;

import com.smartcampus.common.Result;
import com.smartcampus.entity.SysClassroom;
import com.smartcampus.service.SysClassroomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教室Controller
 */
@RestController
@RequestMapping("/api/classroom")
public class ClassroomController {

    @Autowired
    private SysClassroomService classroomService;

    /**
     * 获取所有教室列表
     */
    @GetMapping("/list")
    public Result<List<SysClassroom>> list() {
        List<SysClassroom> list = classroomService.list();
        return Result.success(list);
    }

    /**
     * 根据ID获取教室详情
     */
    @GetMapping("/{id}")
    public Result<SysClassroom> getById(@PathVariable("id") Long id) {
        SysClassroom classroom = classroomService.getById(id);
        if (classroom != null) {
            return Result.success(classroom);
        }
        return Result.error("教室不存在");
    }

    /**
     * 新增教室
     */
    @PostMapping("/add")
    public Result<String> add(@RequestBody SysClassroom classroom) {
        boolean saved = classroomService.save(classroom);
        if (saved) {
            return Result.success("新增成功");
        }
        return Result.error("新增失败");
    }

    /**
     * 更新教室信息
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody SysClassroom classroom) {
        boolean updated = classroomService.updateById(classroom);
        if (updated) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    /**
     * 删除教室
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable("id") Long id) {
        boolean removed = classroomService.removeById(id);
        if (removed) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
