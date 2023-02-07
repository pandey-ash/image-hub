package com.cloud.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloud.entity.User;
import com.cloud.repository.UserRepository;

@Service
public class UserService {
	
	@Value("${bucketName}")
    private String bucketName;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private S3BucketStorageService bucketStorageService;
	
	public User createOrUpdateUser(User user)
	{
		return userRepository.save(user);
	}
	
	public boolean isUsernameExists(String username)
	{
		if(userRepository.existsByUsername(username))
			return true;
		return false;
	}
	
	public User getUserFromUsername(String username)
	{
		List<User> user = userRepository.findByUsername(username);
		if(user == null || user.size() == 0)
			return null;
		return user.get(0);
	}
	
	public User addOrUpdateProfilePic(User user, MultipartFile profilePic, String filePath, String fileName)
	{
		if(user.getProfilePicPath() != null)
			bucketStorageService.deleteFile(filePath, user.getPicName());
		bucketStorageService.uploadFile(filePath, fileName, profilePic);
		user.setProfilePicPath(filePath);
		user.setPicName(fileName);
		user.setUploadDate(new Date());
		user.setModifiedDate(new Date());
		userRepository.save(user);
		return user;
	}
	
	public User deleteProfilePic(User user)
	{
		if(user.getPicName() != null)
			bucketStorageService.deleteFile(user.getProfilePicPath(), user.getPicName());
		user.setProfilePicPath(null);
		user.setPicName(null);
		user.setUploadDate(null);
		user.setModifiedDate(new Date());
		userRepository.save(user);
		return user;
	}
	
	public void verifyUser(User user)
	{
		user.setVerified(true);
		userRepository.save(user);
	}
}
