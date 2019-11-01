package com.baqi.controller;

import com.baqi.annotation.BQAutowired;
import com.baqi.annotation.BQController;
import com.baqi.annotation.BQRequestMapping;
import com.baqi.annotation.BQRequestParam;
import com.baqi.bean.User;
import com.baqi.service.IBaqiService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/***
 *      ┌─┐       ┌─┐
 *   ┌──┘ ┴───────┘ ┴──┐
 *   │                 │
 *   │       ───       │
 *   │  ─┬┘       └┬─  │
 *   │                 │
 *   │       ─┴─       │
 *   │                 │
 *   └───┐         ┌───┘
 *       │         │
 *       │         │
 *       │         │
 *       │         └──────────────┐
 *       │                        │
 *       │                        ├─┐
 *       │                        ┌─┘
 *       │                        │
 *       └─┐  ┐  ┌───────┬──┐  ┌──┘
 *         │ ─┤ ─┤       │ ─┤ ─┤
 *         └──┴──┘       └──┴──┘
 *                神兽保佑
 *               代码无BUG!
 */
@BQController
@BQRequestMapping("/baqi")
public class BaqiController {

    @BQAutowired
    private IBaqiService baqiService;

    public void find(@BQRequestParam("id")Integer id,
                     HttpServletRequest request, HttpServletResponse response) throws IOException {
        User res = baqiService.select(id);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().print(res);
    }

}
