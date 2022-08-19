package com.mars.dor.netty.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author zhuangqingdian
 * @date 2022/8/16
 */
@Controller
@RequestMapping("sand")
public class SandTestController {
    @GetMapping("login")
    public String login(Model model, @RequestParam("userId") String userId) {
        model.addAttribute("userId", userId);
        return "sand";
    }

    //战斗力排行

    //用户对局详情

    //对局记录

}
