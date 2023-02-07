package com.cloud.controller;

import java.security.Principal;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.cloud.entity.ProfilePicResponse;
import com.cloud.entity.User;
import com.cloud.exception.DuplicateUsernameException;
import com.cloud.exception.ExceptionResponse;
import com.cloud.exception.UserNotVerifiedExceptionHandler;
import com.cloud.exception.UsernameUpdateException;
import com.cloud.service.S3BucketStorageService;
import com.cloud.service.UserService;
import com.timgroup.statsd.StatsDClient;

@RestController
public class HomeController
{

	@Autowired
	private UserService userService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private S3BucketStorageService bucketStorageService;
	
	@Value("${bucketName}")
    private String bucketName;
	
	@Autowired
    private AmazonS3 amazonS3Client;
	
	AmazonDynamoDB dynamodbClient;

    AmazonSNS snsClient;

    Long expirationTTL;
    
    @Value("${snstopicArn}")
    private String snstopic;
	
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	
	@Autowired
    private StatsDClient statsDClient;
	
	@GetMapping("/healthz")
	public String getResponse()
	{
		statsDClient.incrementCounter("healthz");
		logger.info("###########Healthz#############");
		return "";
	}
	
	
	
	@PostMapping("/v1/user")
	public ResponseEntity<User> createUser(@Valid @RequestBody User user)
	{
		statsDClient.incrementCounter("createUserApi");
		logger.info("###########create User profile#############");
//		if(userService.isUsernameExists(user.getUsername()))
//			throw new DuplicateUsernameException("");
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		userService.createOrUpdateUser(user);
		dynamodbClient = AmazonDynamoDBClientBuilder.defaultClient();
        logger.info("successfully built dynamodbClient");

        Instant expirationInstant = Instant.now().plusSeconds(300);
        logger.info("expirationInstant=" + expirationInstant);
        expirationTTL = expirationInstant.getEpochSecond();
        logger.info("expirationTTL="+expirationTTL);
        String token = UUID.randomUUID().toString();

        PutItemRequest itemRequest = new PutItemRequest();
        itemRequest.setTableName("csye6225");
        itemRequest.setReturnValues(ReturnValue.ALL_OLD);
        itemRequest.setReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("id", new AttributeValue(user.getUsername()));
        map.put("AccessToken", new AttributeValue(token));
        map.put("TTL", new AttributeValue(expirationTTL.toString()));
        map.put("emailSent", new AttributeValue(expirationTTL.toString()));
        itemRequest.setItem(map);
        dynamodbClient.putItem(itemRequest);
        logger.info("Dynamodb put successful");

        snsClient = AmazonSNSClientBuilder.defaultClient();
        
        JSONObject json = new JSONObject();
        json.put("AccessToken", token);
        json.put("EmailAddress", user.getUsername());
        json.put("MessageType","email");
        
        PublishRequest publishRequest = new PublishRequest()
                .withTopicArn(snstopic)
                .withMessage(json.toString());
        snsClient.publish(publishRequest);
		
		return new ResponseEntity<User>(user, HttpStatus.CREATED);
	}
	
	
	@GetMapping("/v1/user/self")
	public ResponseEntity<User> getUserDetail(Principal principal)
	{
		statsDClient.incrementCounter("getUserDetailApi");
		String username = principal.getName();
		User user = userService.getUserFromUsername(username);
		if(user == null)
			throw new UsernameNotFoundException("Username not found, please login again");
		if(!user.isVerified())
			throw new UserNotVerifiedExceptionHandler("");
		return new ResponseEntity<User>(user, HttpStatus.OK);
	}
	
	@PutMapping("/v1/user/self")
	public ResponseEntity<User> updateUserDetail(Principal principal, @RequestBody User user)
	{
		statsDClient.incrementCounter("updateUserDetailApi");
		String username = principal.getName();
		if(user.getUsername() != null && !username.equals(user.getUsername()))
			throw new UsernameUpdateException("You cannot update username");
		User storedUserDetail = userService.getUserFromUsername(username);
		if(!storedUserDetail.isVerified())
			throw new UserNotVerifiedExceptionHandler("");
		if(user.getFirstName() != null && !"".equals(user.getFirstName().trim()))
			storedUserDetail.setFirstName(user.getFirstName());
		if(user.getLastName() != null && !"".equals(user.getLastName().trim()))
			storedUserDetail.setLastName(user.getLastName());
		if(user.getPassword() != null && !"".equals(user.getPassword().trim()))
		{
			storedUserDetail.setPassword(passwordEncoder.encode(user.getPassword()));
			SecurityContextHolder.getContext().setAuthentication(null);
		}
		storedUserDetail.setModifiedDate(new Date());
		userService.createOrUpdateUser(storedUserDetail);
		return new ResponseEntity<User>(storedUserDetail, HttpStatus.NO_CONTENT);
	}
	
	@PostMapping("/v1/user/self/pic")
	public ResponseEntity<ProfilePicResponse> addOrUpdateProfilePic(Principal principal, @RequestParam("profilePic") MultipartFile profilePic)
	{
		statsDClient.incrementCounter("userProfileApi");
		String username = principal.getName();
		User storedUserDetail = userService.getUserFromUsername(username);
		if(!storedUserDetail.isVerified())
			throw new UserNotVerifiedExceptionHandler("");
		String fileName = profilePic.getOriginalFilename();
		String filePath = bucketName+"/"+storedUserDetail.getId()+"/";
		userService.addOrUpdateProfilePic(storedUserDetail, profilePic, filePath, fileName);
		
		ProfilePicResponse picResponse = new ProfilePicResponse();
		picResponse.setId(storedUserDetail.getId());
		picResponse.setUserId(storedUserDetail.getId());
		picResponse.setUrl(filePath+fileName);
		picResponse.setFileName(fileName);
		picResponse.setUploadDate(storedUserDetail.getUploadDate());
		return new ResponseEntity<ProfilePicResponse>(picResponse, HttpStatus.CREATED);
	}
	
	@GetMapping("/v1/user/self/pic")
	public ResponseEntity<ProfilePicResponse> getProfilePic(Principal principal)
	{
		statsDClient.incrementCounter("getUserProfileApi");
		String username = principal.getName();
		User user = userService.getUserFromUsername(username);
		if(!user.isVerified())
			throw new UserNotVerifiedExceptionHandler("");
		if(user.getPicName() == null)
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		
		ProfilePicResponse picResponse = new ProfilePicResponse();
		picResponse.setId(user.getId());
		picResponse.setUserId(user.getId());
		picResponse.setUrl(user.getProfilePicPath()+user.getPicName());
		picResponse.setFileName(user.getPicName());
		picResponse.setUploadDate(user.getUploadDate());
		return new ResponseEntity<ProfilePicResponse>(picResponse, HttpStatus.OK);
	}
	
	@DeleteMapping("/v1/user/self/pic")
	public ResponseEntity deleteProfilePic(Principal principal)
	{
		statsDClient.incrementCounter("deleteUserProfileApi");
		String username = principal.getName();
		User user = userService.getUserFromUsername(username);
		if(!user.isVerified())
			throw new UserNotVerifiedExceptionHandler("");
		if(user.getPicName() == null)
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		
		userService.deleteProfilePic(user);
		return new ResponseEntity(HttpStatus.NO_CONTENT);
	}
	
	@GetMapping("/v1/verifyUserEmail")
	public ResponseEntity verifyUser(@RequestParam String email, @RequestParam String token)
	{
		ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), "", "");
		if (email == null || "".equals(email.trim()))
		{
			exceptionResponse.setMessage("Email is required");
			return new ResponseEntity(exceptionResponse, HttpStatus.BAD_REQUEST);
		}
		if (token == null || "".equals(token.trim()))
		{
			exceptionResponse.setMessage("Token is required");
			return new ResponseEntity(exceptionResponse, HttpStatus.BAD_REQUEST);
        }
		email = email.trim();
		token = token.trim();
		dynamodbClient = AmazonDynamoDBClientBuilder.defaultClient();
		DynamoDB dynamoDB = new DynamoDB(dynamodbClient);
		Table table = dynamoDB.getTable("csye6225");
		//GetItemSpec getItemSpec = new GetItemSpec().withPrimaryKey("id", email);
		Item item = table.getItem("id", email);
		if (item == null || !item.get("AccessToken").equals(token))
		{
			exceptionResponse.setMessage("Invalid token");
			return new ResponseEntity(exceptionResponse, HttpStatus.BAD_REQUEST);
		}
		if (Long.parseLong(item.get("TTL").toString()) < Long.parseLong(String.valueOf(Instant.now().getEpochSecond()))) {
			exceptionResponse.setMessage("Token expire");
			return new ResponseEntity(exceptionResponse, HttpStatus.BAD_REQUEST);
        }
		User user = userService.getUserFromUsername(email.trim());
		userService.verifyUser(user);
		return new ResponseEntity("User is verified", HttpStatus.OK);
	}
	
	
}
