package com.lin.blog.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lin.blog.dao.mapper.ArticleMapper;
import com.lin.blog.dao.pojo.Article;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ThreadService {
    //异步执行
    //期望此操作在线程池执行，不会影响原有的主线程
    @Async("taskExecutor")
    public void updateArticleViewCount(ArticleMapper articleMapper, Article article) {

        int viewCounts = article.getViewCounts();
        Article articleUpdate=new Article();
        articleUpdate.setViewCounts(viewCounts+1);
        LambdaUpdateWrapper<Article> updateWrapper=new LambdaUpdateWrapper<>();
        updateWrapper.eq(Article::getId,article.getId());
        //设置一个 为了在多线程的环境下
        updateWrapper.eq(Article::getViewCounts,viewCounts);
        //update   article set view_count=100 where view_count=99 id=11
        articleMapper.update(articleUpdate,updateWrapper);
//        try {
//            Thread.sleep(5000);
//            System.out.println("更新完成....");
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }
}
