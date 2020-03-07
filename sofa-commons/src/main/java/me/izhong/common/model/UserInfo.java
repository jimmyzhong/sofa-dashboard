package me.izhong.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;

@Getter
@Setter
public class UserInfo implements Serializable {

    static final long serialVersionUID = 1L;

    private Long userId;
    private String loginName;
    private String loginIp;
    private String userName;
    private String nickName;
    private String email;
    private String phoneNumber;
    private String sex;
    private String avatar;
    private String status;
    private Date createTime;
    private Date updateTime;
    private String deptName;
    private Long deptId;

    private String createBy;
    private String updateBy;

    @JSONField(serialize = false,deserialize = false)
    private boolean hasAllDeptPerm = false;

    @JSONField(serialize = false,deserialize = false)
    private Set<String> roles;

    @JSONField(serialize = false,deserialize = false)
    private Set<String> perms;

    public UserInfo() {

    }

    @Getter
    @Setter
    @JSONField(serialize = false,deserialize = false)
    private static Map<Long,String> deptIdNames = new HashMap<>();


    @JSONField(serialize = false,deserialize = false)
    private Map<String,Set<Long>> scopes = new HashMap<>();

    public UserInfo addScopeData(String perm, Long deptId){
        if(StringUtils.isBlank(perm) || deptId == null)
            return this;
        Set<Long> s = scopes.get(perm);
        if(s == null) {
            s = new HashSet<>();
            scopes.put(perm,s);
        }
        if(!s.contains(deptId)) {
            s.add(deptId);
        }
        return this;
    }

    public UserInfo addScopeData(String perm, List<Long> deptId){
        for(Long id: deptId){
            addScopeData(perm,id);
        }
        return this;
    }

    public Set<Long> getScopeData(String perm){
        if(StringUtils.isBlank(perm))
            return new HashSet<>();
        return scopes.get(perm);
    }

    public boolean hashScopePermission(String perm, Long deptId){
        if(hasAllDeptPerm)
            return true;
        Set<Long> s = scopes.get(perm);
        if(s != null && s.contains(deptId))
            return true;
        return false;
    }

    public void checkScopePermission(String perm, Long deptId){
        if(hasAllDeptPerm)
            return;
        if(!hashScopePermission(perm,deptId)) {
            String notice = deptIdNames.get(deptId);
            throw new RuntimeException("缺少数据对应的部门权限! 权限:[" + perm + "],部门:" + notice + "[" + deptId + "]");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof UserInfo) {

            if (o == this) {
                return true;
            }

            UserInfo anotherUser = (UserInfo) o;
            return userId.equals(anotherUser.userId);
        } else {
            return false;
        }

    }
}
