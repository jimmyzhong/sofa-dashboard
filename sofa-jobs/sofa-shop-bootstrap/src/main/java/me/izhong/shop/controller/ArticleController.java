package me.izhong.shop.controller;

import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.shop.entity.Articles;
import me.izhong.shop.service.IArticlesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@AjaxWrapper
@RequestMapping("/api/article")
public class ArticleController {

    @Autowired
    private IArticlesService articlesService;

    @RequestMapping(value = "/type/{type}")
    public Articles contentType(@PathVariable("type") String type) {
        return articlesService.findByType(type);
    }

}
