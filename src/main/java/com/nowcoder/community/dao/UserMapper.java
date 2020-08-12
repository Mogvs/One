package com.nowcoder.community.dao;

import com.nowcoder.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author shkstart
 * @create 2020-08-10 20:50
 */
@Mapper
public interface UserMapper {

  User selectById(int id);

  User selectByName(String username);

  User selectByEmail(String email);

  int insertUser(User user);

  int updateStatus(int id, int status);

  //int updateStatus(int id, String status);

  int updateHeader(int id, String headerUrl);

  int updatePassword(int id, String password);


}
