package com.cloud.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.cloud.entity.User;
import com.cloud.repository.UserRepository;

@Component
public class ApplicationUsernamePwdAuthenticationProvider implements AuthenticationProvider {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException
	{
		String username = authentication.getName();
		String password = authentication.getCredentials().toString();
		List<User> users = userRepository.findByUsername(username);
		if(users.size() > 0)
		{
			if(passwordEncoder.matches(password, users.get(0).getPassword()))
			{
				return new UsernamePasswordAuthenticationToken(username, password, null);
			}
			else
			{	
				throw new BadCredentialsException("Invalid password!");
			}
		}
		else
		{
			throw new BadCredentialsException("No user registered with this details!");
		}
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

}
