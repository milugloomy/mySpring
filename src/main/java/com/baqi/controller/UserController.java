package com.baqi.controller;

import com.baqi.annotation.BQAutowired;
import com.baqi.annotation.BQController;
import com.baqi.annotation.BQRequestMapping;
import com.baqi.annotation.BQRequestParam;
import com.baqi.bean.User;
import com.baqi.service.UserService;
import com.baqi.util.ResEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
@BQRequestMapping("/user")
public class UserController {

    @BQAutowired
    private UserService userService;

    @BQRequestMapping("/selectList")
    public ResEntity selectList(){
        List<User> list = userService.selectList();
        return new ResEntity(list);
    }

    @BQRequestMapping("/select")
    public ResEntity select(Integer id, HttpServletRequest request){
        User user = userService.select(id);
        return new ResEntity(user);
    }

    @BQRequestMapping("/update")
    public ResEntity update(@BQRequestParam("id")Integer id,
                            @BQRequestParam("name")String name,
                            @BQRequestParam("age")Integer age){
        User user= new User(id, name, age);
        userService.update(user);
        return new ResEntity();
    }

    @BQRequestMapping("/delete")
    public ResEntity delete(@BQRequestParam("id")Integer id){
        userService.delete(id);
        return new ResEntity();
    }

}
