package com.lin.blog.controller;

import com.lin.blog.common.aop.LogAnnotation;
import com.lin.blog.common.cache.Cache;
import com.lin.blog.dao.pojo.Article;
import com.lin.blog.service.ArticleService;
import com.lin.blog.vo.Result;
import com.lin.blog.vo.params.ArticleParam;
import com.lin.blog.vo.params.PageParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
//json数据交互

@RestController
@RequestMapping("articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;
    /**
     * 首页 文章列表
     * @param pageParams
     * @return
     */
    @PostMapping
    //加上此注解，代表对此接口记录日志
    @LogAnnotation(module="文章",operator="获取文章列表")
    public Result listArticle(@RequestBody PageParams pageParams){
        return articleService.listArticle(pageParams);
    }

    /**
     * 首页 最热文章
     * @return
     */
    @PostMapping("hot")
    @Cache(expire = 5*60*1000,name = "hot_article")
    public Result hotArticle(){
        int limit=5;
        return articleService.hotArticle(limit);
    }
    /**
     * 首页 最新文章
     * @return
     */
    @PostMapping("new")
    @Cache(expire = 5*60*1000,name = "newArticles")
    public Result newArticles(){
        int limit=5;
        return articleService.newArticles(limit);
    }
    /**
     * 首页 文章归档
     * @return
     */
    @PostMapping("listArchives")
    @Cache(expire = 5*60*1000,name = "listArchives")
    public Result listArchives(){
        int limit=5;
        return articleService.listArchives();
    }
    /**
     *
     */
   @PostMapping("view/{id}")
    public Result findArticleById(@PathVariable("id") Long articleId){
       return articleService.findArticleById(articleId);
   }

   ///articles/publish
   @PostMapping("publish")
    public Result publish(@RequestBody ArticleParam articleParam){

       return articleService.publish(articleParam);
   }

}
