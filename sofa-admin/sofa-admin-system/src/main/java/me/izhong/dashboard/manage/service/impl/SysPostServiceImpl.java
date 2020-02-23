package me.izhong.dashboard.manage.service.impl;

import me.izhong.dashboard.manage.dao.UserPostDao;
import me.izhong.dashboard.manage.service.SysUserService;
import me.izhong.db.common.service.CrudBaseServiceImpl;
import me.izhong.dashboard.manage.dao.PostDao;
import me.izhong.dashboard.manage.entity.SysPost;
import me.izhong.dashboard.manage.entity.SysUser;
import me.izhong.dashboard.manage.entity.SysUserPost;
import me.izhong.common.exception.BusinessException;
import me.izhong.dashboard.manage.service.SysPostService;
import me.izhong.common.util.Convert;
import me.izhong.db.common.util.CriteriaUtil;
import me.izhong.dashboard.manage.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

@Service
public class SysPostServiceImpl extends CrudBaseServiceImpl<Long, SysPost> implements SysPostService{

    @Autowired
    private PostDao postDao;

    @Autowired
    private UserPostDao userPostDao;

    @Autowired
    private SysUserService userService;
    /**
     * 根据用户ID查询岗位
     *
     * @param userId 用户ID
     * @return 岗位列表
     */
    @Override
    public List<SysPost> selectPostsByUserId(Long userId) {
        List<SysPost> userSysPosts = doSelectPostsByUserId(userId);
        List<SysPost> sysPosts = postDao.findAll();
        for (SysPost sysPost : sysPosts) {
            for (SysPost userRole : userSysPosts) {
                if (sysPost.getPostId().longValue() == userRole.getPostId().longValue()) {
                    sysPost.setFlag(true);
                    break;
                }
            }
        }
        return sysPosts;
    }

    /**
     * 批量删除岗位信息
     *
     * @param ids 需要删除的数据ID
     * @throws Exception
     */
    @Transactional
    @Override
    public long deleteByPIds(String ids) throws BusinessException {
        //校验下
        Long[] postIds = Convert.toLongArray(ids);
        for (Long postId : postIds) {
            SysPost sysPost = selectByPId(postId);
            if (countUserPostByPostId(postId) > 0) {
                List<SysUser> sysUsers = doSelectUserByPostId(postId,30);
                List<String> userNames = sysUsers.stream().map(e->e.getLoginName()).collect(Collectors.toList());
                throw BusinessException.build(StringUtil.format("岗位[{}]已分配给{},不能删除", sysPost.getPostName(),userNames));
            }
        }

        return super.deleteByPIds(ids);
    }

    /**
     * 通过岗位ID查询岗位使用数量
     *
     * @param postId 岗位ID
     * @return 结果
     */
    @Override
    public long countUserPostByPostId(Long postId) {
        return doCountUserPostByPostId(postId);
    }

    @Override
    public List<SysUser> selectUserByPostId(Long postId, long limit) {
        return doSelectUserByPostId(postId,limit);
    }

    /**
     * 校验岗位名称是否唯一
     *
     * @param sysPost 岗位信息
     * @return 结果
     */
    @Override
    public boolean checkPostNameUnique(SysPost sysPost) {
        Long postId = sysPost.getPostId() == null ? -1L : sysPost.getPostId();
        SysPost info = postDao.findByPostName(sysPost.getPostName());
        if (info != null && info.getPostId().longValue() != postId.longValue()) {
            return false;
        }
        return true;
    }

    /**
     * 校验岗位编码是否唯一
     *
     * @param sysPost 岗位信息
     * @return 结果
     */
    @Override
    public boolean checkPostCodeUnique(SysPost sysPost) {
        Long postId = sysPost.getPostId() == null ? -1L : sysPost.getPostId();
        SysPost info = postDao.findByPostCode(sysPost.getPostCode());
        if (info != null && info.getPostId().longValue() != postId.longValue()) {
            return false;
        }
        return true;
    }

    @Override
    public long deleteAuthUsers(Long userId) {
        Assert.notNull(userId,"");
        return userPostDao.deleteAllByUserId(userId);
    }

    @Transactional
    @Override
    public long removePostInfo(String ids) {
        Long[] postIds = Convert.toLongArray(ids);
        for (Long postId : postIds) {
            SysPost sysPost = selectByPId(postId);
            List<SysUser> userPosts = doSelectUserByPostId(postId, 10);
            if (userPosts!= null && userPosts.size() > 0) {
                List<String> loginNames = userPosts.stream().map(e->e.getLoginName()).collect(Collectors.toList());
                throw BusinessException.build("[" + sysPost.getPostName() + "]已分配给用户"+loginNames+"不能删除");
            }
        }
        return removeByPIds(ids);
    }

    private List<SysPost> doSelectPostsByUserId(Long userId) {
        List<AggregationOperation> aggregationOperations = new ArrayList<>();

        LookupOperation lookupOperationUserMenu = LookupOperation.newLookup().
                from("sys_user_post").
                localField("postId").
                foreignField("postId").
                as("up");
        aggregationOperations.add(lookupOperationUserMenu);

        //查询某个用户的权限
        aggregationOperations.add(match(Criteria.where("up.userId").is(userId)));

        //只显示正常
        aggregationOperations.add(match(CriteriaUtil.notDeleteCriteria()));

        aggregationOperations.add(sort(Sort.Direction.ASC, "postSort"));


        Aggregation aggregation = Aggregation.newAggregation(aggregationOperations);
        return mongoTemplate.aggregate(aggregation, SysPost.class, SysPost.class).getMappedResults();
    }

    private long doCountUserPostByPostId(Long postId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("postId").is(postId));
        return mongoTemplate.count(query, SysUserPost.class);
    }

    private List<SysUser> doSelectUserByPostId(Long postId,long limit) {
        Assert.notNull(postId,"");
        List<AggregationOperation> aggregationOperations = new ArrayList<>();

        LookupOperation lookupOperationUserMenu = LookupOperation.newLookup().
                from("sys_user_post").
                localField("userId").
                foreignField("userId").
                as("up");
        aggregationOperations.add(lookupOperationUserMenu);

        aggregationOperations.add(match(Criteria.where("up.postId").is(postId)));
        aggregationOperations.add(limit(limit));

        Aggregation aggregation = Aggregation.newAggregation(aggregationOperations);
        return mongoTemplate.aggregate(aggregation, SysUser.class, SysUser.class).getMappedResults();
    }

    private long doCountUserPostByUserId(Long userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        return mongoTemplate.count(query, SysUserPost.class);
    }

}
