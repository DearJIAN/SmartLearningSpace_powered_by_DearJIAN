package com.smartcampus;

import com.smartcampus.entity.VisualStatsLog;
import com.smartcampus.service.VisualStatsLogService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 智学空间 - 启动类
 */
@SpringBootApplication
@MapperScan("com.smartcampus.mapper")
public class SmartCampusApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartCampusApplication.class, args);
        System.out.println("======================================");
        System.out.println("  智学空间后端服务启动成功!");
        System.out.println("  端口: 8080");
        System.out.println("======================================");
    }

    /**
     * 启动时生成符合课表规律的模拟数据
     */
    @Bean
    public CommandLineRunner dataInitializer(VisualStatsLogService visualStatsLogService,
            com.smartcampus.service.SysClassroomService sysClassroomService) {
        return args -> {
            System.out.println("正在清空旧数据并生成新的模拟数据...");

            // 1. 初始化教室数据 (101-105)
            sysClassroomService.remove(null);
            List<com.smartcampus.entity.SysClassroom> classroomList = new ArrayList<>();
            for (long i = 101; i <= 105; i++) {
                com.smartcampus.entity.SysClassroom classroom = new com.smartcampus.entity.SysClassroom();
                classroom.setRoomId(i);
                classroom.setRoomName(i + " 教室");
                classroom.setCapacity(180);
                classroom.setCameraUrl("mock://stream/" + i);
                classroom.setIsActive(1);
                classroomList.add(classroom);
            }
            sysClassroomService.saveBatch(classroomList);
            System.out.println("教室数据初始化完成: 101-105");

            // 2. 清空旧统计日志
            visualStatsLogService.remove(null);

            Random random = new Random();
            List<VisualStatsLog> allLogs = new ArrayList<>();

            // 3. 为每个教室生成过去24小时的模拟数据，每30分钟一条
            for (long roomId = 101; roomId <= 105; roomId++) {
                Calendar calendar = Calendar.getInstance();
                for (int i = 47; i >= 0; i--) {
                    calendar.setTime(new Date());
                    calendar.add(Calendar.MINUTE, -i * 30);

                    // 获取当前小时和分钟
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    int minute = calendar.get(Calendar.MINUTE);

                    // 判断是否在上课时间
                    boolean isClassTime = isInClassTime(hour, minute);

                    VisualStatsLog log = new VisualStatsLog();
                    log.setRoomId(roomId);

                    int personCount;
                    int phoneCount;

                    if (isClassTime) {
                        // 上课时间：80-160人 (接近180容量)
                        personCount = 80 + random.nextInt(81);
                        phoneCount = 2 + random.nextInt(10);
                    } else {
                        // 课间/休息：0-15人
                        personCount = random.nextInt(16);
                        phoneCount = personCount > 0 ? random.nextInt(Math.min(5, personCount + 1)) : 0;
                    }

                    log.setPersonCount(personCount);
                    log.setPhoneCount(phoneCount);

                    // 计算专注度指数
                    double focusIndex = 0.0;
                    if (personCount > 0) {
                        focusIndex = (double) (personCount - phoneCount) / personCount;
                    }
                    log.setFocusIndex(Math.round(focusIndex * 100.0) / 100.0);
                    log.setCreateTime(calendar.getTime());

                    allLogs.add(log);
                }
            }

            visualStatsLogService.saveBatch(allLogs);
            System.out.println("模拟数据生成完成，共为 5 个教室生成 " + allLogs.size() + " 条记录");
        };
    }

    /**
     * 判断指定时间是否在上课时间段内
     * 上午: 08:00 - 11:50
     * 下午: 14:30 - 18:00
     * 晚自习: 18:40 - 22:00
     */
    private boolean isInClassTime(int hour, int minute) {
        int timeInMinutes = hour * 60 + minute;

        // 上午 08:00 - 11:50
        if (timeInMinutes >= 8 * 60 && timeInMinutes < 11 * 60 + 50) {
            return true;
        }

        // 下午 14:30 - 18:00
        if (timeInMinutes >= 14 * 60 + 30 && timeInMinutes < 18 * 60) {
            return true;
        }

        // 晚自习 18:40 - 22:00
        if (timeInMinutes >= 18 * 60 + 40 && timeInMinutes < 22 * 60) {
            return true;
        }

        return false;
    }
}
