package com.lin.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lin.blog.dao.dos.Archives;
import com.lin.blog.dao.mapper.ArticleBodyMapper;
import com.lin.blog.dao.mapper.ArticleMapper;
import com.lin.blog.dao.mapper.ArticleTagMapper;
import com.lin.blog.dao.mapper.SysUserMapper;
import com.lin.blog.dao.pojo.*;
import com.lin.blog.service.*;
import com.lin.blog.utils.UserThreadLocal;
import com.lin.blog.vo.*;
import com.lin.blog.vo.params.ArticleParam;
import com.lin.blog.vo.params.PageParams;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private TagService tagService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private ArticleTagMapper articleTagMapper;
    @Override
    public Result listArticle(PageParams pageParams) {
        Page<Article> page=new Page<>(pageParams.getPage(),pageParams.getPageSize());
        IPage<Article> articleIPage = articleMapper.listArticle(page, pageParams.getCategoryId()
                , pageParams.getTagId()
                , pageParams.getYear()
                , pageParams.getMonth());
        List<Article> records = articleIPage.getRecords();
        return Result.success(copyList(records,true,true));


    }
//    @Override
//    public Result listArticle(PageParams pageParams) {
//        /**
//         * 分页查询article数据库表
//         */
//        //当前查询的页数，以及每页查询的数量
//        Page<Article> page=new Page<>(pageParams.getPage(),pageParams.getPageSize());
//        LambdaQueryWrapper<Article> queryWrapper=new LambdaQueryWrapper<>();
//        if (pageParams.getCategoryId()!=null)
//        {
//            queryWrapper.eq(Article::getCategoryId,pageParams.getCategoryId());
//        }
//        List<Long> articleIdList=new ArrayList<>();
//        if (pageParams.getTagId()!=null)
//        {
//            LambdaQueryWrapper<ArticleTag> articleTagLambdaQueryWrapper=new LambdaQueryWrapper<>();
//            articleTagLambdaQueryWrapper.eq(ArticleTag::getTagId,pageParams.getTagId());
//            List<ArticleTag> articleTags = articleTagMapper.selectList(articleTagLambdaQueryWrapper);
//            for (ArticleTag articleTag : articleTags) {
//                articleIdList.add(articleTag.getArticleId());
//            }
//            if (articleIdList.size()>0){
//                queryWrapper.in(Article::getId,articleIdList);
//            }
//        }
//        //是否置顶进行排序
//        //order by create_data desc
//        queryWrapper.orderByDesc(Article::getWeight,Article::getCreateDate);//倒叙进行排列
//        Page<Article> articlePage = articleMapper.selectPage(page, queryWrapper);
//        List<Article> records = articlePage.getRecords();
//        //不能直接返回list
//
//        List<ArticleVo> articleVoList=copyList(records,true,true);
//        return Result.success(articleVoList);
//    }

    /**
     * 首页-最热文章
     * @param limit
     * @return
     */
    @Override
    public Result hotArticle(int limit) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getViewCounts);
        queryWrapper.select(Article::getId,Article::getTitle);
        queryWrapper.last("limit " + limit);
        //select id,title from article order by view_counts desc limit  5
        List<Article> articles = articleMapper.selectList(queryWrapper);
        return Result.success(copyList(articles,false,false));
    }
    /**
     * 首页-最新文章
     * @param limit
     * @return
     */
    @Override
    public Result newArticles(int limit) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getCreateDate);
        queryWrapper.select(Article::getId,Article::getTitle);
        queryWrapper.last("limit " + limit);
        //select id,title from article order by create_data desc limit  5
        List<Article> articles = articleMapper.selectList(queryWrapper);
        return Result.success(copyList(articles,false,false));
    }

    /**
     * 归档
     * @return
     */
    @Override
    public Result listArchives() {
        List<Archives> archives=articleMapper.listArchives();
        return Result.success(archives);
    }

    @Autowired
    private ThreadService threadService;

    @Override
    public Result findArticleById(Long articleId) {
        /**
         * 根据id查询文章信息
         * 根据bodyid和caregoryid
         *
         */
        Article article=this.articleMapper.selectById(articleId);
        ArticleVo articleVo = copy(article, true, true,true,true);
        //看完需要增加阅读量
        //使用线程池，可以吧更新操作扔到线程池中执行，和主线程不相关
        threadService.updateArticleViewCount(articleMapper,article);
        return Result.success(articleVo);
    }

    /**
     * 文章发布
     * @param articleParam
     * @return
     */
    @Override
    public Result publish(ArticleParam articleParam) {
        //此接口要加入到登录拦截
        SysUser sysUser = UserThreadLocal.get();
        /**
         * 发布文章。目的是构建article对象
         * 作者id ，当前登录用户
         * 标签，要将标签加入到关联列表当中
         * body内容存储article bodyid
         */
        Article article=new Article();
        article.setAuthorId(sysUser.getId());
        article.setWeight(Article.Article_Common);
        article.setViewCounts(0);
        article.setTitle(articleParam.getTitle());
        article.setSummary(articleParam.getSummary());
        article.setCommentCounts(0);
        article.setCreateDate(System.currentTimeMillis());
        article.setCategoryId(Long.parseLong(articleParam.getCategory().getId()));
        //插入之后会生成一个文章id
        this.articleMapper.insert(article);
        List<TagVo> tags = articleParam.getTags();
        if (tags!=null)
        {
            for (TagVo tag : tags) {
                Long articleId = article.getId();
                ArticleTag articleTag=new ArticleTag();
                articleTag.setTagId(Long.parseLong(tag.getId()));
                articleTag.setArticleId(articleId);
                articleTagMapper.insert(articleTag);
            }
        }
        //body
        ArticleBody articleBody=new ArticleBody();
        articleBody.setArticleId(article.getId());
        articleBody.setContent(articleParam.getBody().getContent());
        articleBody.setContentHtml(articleParam.getBody().getContentHtml());
        articleBodyMapper.insert(articleBody);
        article.setBodyId(articleBody.getId());
        articleMapper.updateById(article);
        Map<String,String> map=new HashMap<>();
        map.put("id",article.getId().toString());
        return Result.success(map);
    }


    private List<ArticleVo> copyList(List<Article> records,boolean isTag,boolean isAuthor) {
        List<ArticleVo> articleVoList=new ArrayList<>();
        for (Article record : records) {
            articleVoList.add(copy(record,isTag,isAuthor,false,false));

        }
        return articleVoList;
    }
    private List<ArticleVo> copyList(List<Article> records,boolean isTag,boolean isAuthor,boolean isBody,boolean isCategory) {
        List<ArticleVo> articleVoList=new ArrayList<>();
        for (Article record : records) {
            articleVoList.add(copy(record,isTag,isAuthor,isBody,isCategory));

        }
        return articleVoList;
    }


    @Autowired
    private CategoryService categoryService;

        private ArticleVo copy(Article article, boolean isTag, boolean isAuthor, boolean isBody, boolean isCategory){
        ArticleVo articleVo = new ArticleVo();
        articleVo.setId(String.valueOf(article.getId()));
        BeanUtils.copyProperties(article,articleVo);

        articleVo.setCreateDate(new DateTime(article.getCreateDate()).toString("yyyy-MM-dd HH:mm"));
//        并不是所有的接口都需要标签，作者信息
        if (isTag)
        {
            Long articleId = article.getId();
            articleVo.setTags(tagService.findTagsByArticleId(articleId));
        }
        if (isAuthor)
        {
            Long authorId = article.getAuthorId();

            articleVo.setAuthor(sysUserService.findUserById(authorId).getNickname());
        }
        if (isBody){
//            Long bodyId=article.getBodyId();
//            articleVo.setBody(findArticleBodyById(bodyId));
            ArticleBodyVo articleBody = findArticleBody(article.getId());
            articleVo.setBody(articleBody);
        }

        if (isCategory){
//            Long categoryId=article.getCategoryId();
//            articleVo.setCategory(categoryService.findCategoryById(categoryId));
            CategoryVo categoryVo = findCategory(article.getCategoryId());
            articleVo.setCategory(categoryVo);
        }
        return articleVo;
    }

    @Autowired
    private ArticleBodyMapper articleBodyMapper;

    private ArticleBodyVo findArticleBodyById(Long bodyId) {
        ArticleBody articleBody = articleBodyMapper.selectById(bodyId);
        ArticleBodyVo articleBodyVo=new ArticleBodyVo();
        articleBodyVo.setContent(articleBody.getContent());
        return articleBodyVo;
    }


    private CategoryVo findCategory(Long categoryId) {
        return categoryService.findCategoryById(categoryId);
    }
    private ArticleBodyVo findArticleBody(Long articleId) {
        LambdaQueryWrapper<ArticleBody> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleBody::getArticleId,articleId);
        ArticleBody articleBody = articleBodyMapper.selectOne(queryWrapper);
        ArticleBodyVo articleBodyVo = new ArticleBodyVo();
        articleBodyVo.setContent(articleBody.getContent());
        return articleBodyVo;
    }





}
