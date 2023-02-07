package com.csye6225.lambda;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class EmailEvent {

    //@Value("${tableName}")
   // private String tableName;
	
	AmazonDynamoDB dynamodbClient;
	
	private static final Logger logger = LoggerFactory.getLogger(EmailEvent.class);

    private static final String EMAIL_SUBJECT="Verification needed";


    private static final String SENDER_EMAIL = System.getenv("SenderEmail");

    public Object handleRequest(SNSEvent request, Context context){
        
        

        if (request.getRecords() == null) {
        	logger.error("There are no records available");
            return null;
        }
        
        String messageFromSQS =  request.getRecords().get(0).getSNS().getMessage();
        JsonObject jsonObject = JsonParser.parseString(messageFromSQS).getAsJsonObject();
        
        
        logger.info("messageFromSQS="+messageFromSQS);
        String emailRecipient = (String) jsonObject.get("EmailAddress").getAsString();
        String accessToken = (String) jsonObject.get("AccessToken").getAsString();
        
        logger.info("emailRecipient="+emailRecipient);
        logger.info("accessToken="+accessToken);
        
        dynamodbClient = AmazonDynamoDBClientBuilder.defaultClient();
		DynamoDB dynamoDB = new DynamoDB(dynamodbClient);
		Table table = dynamoDB.getTable("token_sent_email");
		logger.info("connected to token sent table");
		//GetItemSpec getItemSpec = new GetItemSpec().withPrimaryKey("id", email);
		Item item = table.getItem("id", emailRecipient);
		logger.info("item="+item);
		if(item != null)
			return null;
        
        String emailBody = "Thank you for registering at us\n.Please click on the below verification link to confirm your registration: \n";
        emailBody += "https://demo.ashish-pandey.me/v1/verifyUserEmail?email="+emailRecipient+"&token="+accessToken;
        
        Content content = new Content().withData(emailBody);
        Body body = new Body().withText(content);
        try {
        	logger.info("Before AmazonSimpleEmailService");
            AmazonSimpleEmailService client =
                    AmazonSimpleEmailServiceClientBuilder.standard()
                            .withRegion(Regions.US_EAST_1).build();
            logger.info("Before SendEmailRequest");
            SendEmailRequest emailRequest = new SendEmailRequest()
                    .withDestination(
                            new Destination().withToAddresses(emailRecipient))
                    .withMessage(new Message()
                            .withBody(body)
                            .withSubject(new Content()
                                    .withCharset("UTF-8").withData(EMAIL_SUBJECT)))
                    .withSource(SENDER_EMAIL);
            client.sendEmail(emailRequest);
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
        
        
        item = new Item().withPrimaryKey("id", emailRecipient);
        logger.info("before item put");
        table.putItem(item);
        logger.info("item put successfully");
        return null;
    }
    
    
}
