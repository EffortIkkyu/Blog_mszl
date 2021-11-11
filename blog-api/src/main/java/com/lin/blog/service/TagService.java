package com.lin.blog.service;


import com.lin.blog.vo.Result;
import com.lin.blog.vo.TagVo;

import java.util.List;

public interface TagService {


    List<TagVo> findTagsByArticleId(Long articleId);

    Result hots(int limit);

    /**
     * 查询所有文章标签
     * @return
     */
    Result findAll();

    Result findAllDetail();

    Result findDetailById(Long id);
}
