package com.lwohvye.springboot.dubboconsumer.controller;

import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
import com.github.pagehelper.PageInfo;
import com.lwohvye.springboot.dubboconsumer.common.listener.UserLogExcelListener;
import com.lwohvye.springboot.dubboconsumer.common.util.ResultModel;
import com.lwohvye.springboot.dubbointerface.common.util.excel.ExcelUtil;
import com.lwohvye.springboot.dubbointerface.entity.UserLog;
import com.lwohvye.springboot.dubbointerface.service.UserLogService;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @author Hongyan Wang
 * @packageName
 * @className UserLogController
 * @description
 * @date 2019/12/18 16:42
 */
@RestController
@RequestMapping("/userLog")
public class UserLogController {

    @Reference(version = "${lwohvye.service.version}")
    private UserLogService userLogService;

    /**
     * @return com.lwohvye.springboot.dubboconsumer.common.util.ResultModel
     * @description 获取日志列表
     * @params [username, searchTime, pages, limits]
     * @author Hongyan Wang
     * @date 2019/12/18 16:54
     */
    @ApiOperation(value = "获取日志列表", notes = "获取日志列表，包含根据用户名及操作时间分页查询")
    @RequestMapping(value = "/list", method = {RequestMethod.GET, RequestMethod.POST})
//    @ResponseBody
    public ResultModel<PageInfo<UserLog>> list(String username, String searchTime, int page, int pageSize) {
//        JSONObject json = new JSONObject();
//
        String startDate = null;
        String endDate = null;
        if (!StringUtils.isEmpty(searchTime)) {
            String[] searchTimes = searchTime.split(" - ");
            startDate = searchTimes[0];
            endDate = searchTimes[1];
        }
//
//        PageInfo<UserLog> pageInfo = userLogService.list(username, startDate, endDate, page, pageSize);
//
//        json.put("flag", true);
//        json.put("result", pageInfo);
//        return json.toJSONString();
        return new ResultModel<>(userLogService.list(username, startDate, endDate, page, pageSize));
    }

    /**
     * @description 文件下载
     * @params [response]
     * @return void
     * @author Hongyan Wang
     * @date 2020/2/28 13:28
     */
    @GetMapping("/download")
    public void download(HttpServletResponse response) throws IOException {
//        设置响应信息
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        var fileName = URLEncoder.encode("日志信息" + IdUtil.simpleUUID(), StandardCharsets.UTF_8);
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        var list = this.list(null, null, 1, 40);
//        下载文件
        new ExcelUtil<UserLog>().download(response, list.getData().getList(), new UserLog(), "第一栏");
    }

    @PostMapping("/upload")
    public String update(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(),UserLog.class,new UserLogExcelListener(userLogService)).sheet().doRead();
        return "success";
    }

}
