package com.cloud.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.cloud.entity.User;

@Repository
public interface UserRepository extends PagingAndSortingRepository<User, Integer> {
	Boolean existsByUsername(String username);
	
	List<User> findByUsername(String username);
}
