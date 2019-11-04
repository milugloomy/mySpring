package com.baqi.service;

import com.baqi.annotation.BQPostConstruct;
import com.baqi.annotation.BQService;
import com.baqi.bean.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
@BQService
public class UserService {

    private Map<Integer, User> map;

    @BQPostConstruct
    public void init() {
        map = new HashMap<>();
        map.put(1, new User(1, "哈哈", 29));
        map.put(2, new User(2, "拉拉", 30));
    }

    public List<User> selectList() {
        List<User> list = new ArrayList();
        map.entrySet().forEach(item -> {
            list.add(item.getValue());
        });
        return list;
    }

    public User select(Integer id) {
        return map.get(id);
    }

    public void insert(User user) {
        map.put(user.getId(), user);
    }

    public void update(User user) {
        map.put(user.getId(), user);
    }

    public void delete(Integer id) {
        map.remove(id);
    }
}
