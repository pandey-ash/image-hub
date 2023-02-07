package com.cloud.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cloud.entity.SecurityUser;
import com.cloud.entity.User;
import com.cloud.repository.UserRepository;

@Service
public class ApplicationUserDetails implements UserDetailsService 
{

	@Autowired
	private UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
	{
		List<User> user = userRepository.findByUsername(username);
		if (user.size() == 0) {
			throw new UsernameNotFoundException("User details not found for the user : " + username);
		}
		return new SecurityUser(user.get(0));
	}

}
