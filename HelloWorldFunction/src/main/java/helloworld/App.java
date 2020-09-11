package helloworld;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import java.util.ArrayList;

import software.amazon.codeguruprofilerjavaagent.LambdaProfiler;


import java.util.UUID;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<Object, Object> {

    @Override
    public Object handleRequest(final Object input, final Context context) {
      return LambdaProfiler.profile(input, context, this::myHandlerFunction);
    }
    public Object myHandlerFunction(final Object input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");
        try {
            final String pageContents = this.getPageContents("https://checkip.amazonaws.com");
            UUID id = UUID.randomUUID();
            final String ddbtestput = this.PutItem("HelloWorldTable", "id", id.toString(), "field1", "stringValue1");
            final String ddbtestget = this.GetItem("HelloWorldTable", "id", id.toString() );

            String output = String.format("{ \"message\": \"hello world\", \"location\": \"%s\" \n \"put\":\"%s\"-\"%s\" \n \"get\":\"%s\"-\"%s\"}", pageContents,id.toString(),ddbtestput,id.toString(),ddbtestget);
            return new GatewayResponse(output, headers, 200);
        } catch (IOException e) {
            return new GatewayResponse("{}", headers, 500);
        }
    }

    private String getPageContents(String address) throws IOException{
        URL url = new URL(address);
        try(BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

    private String GetItem(String TableName, String PrimaryKey, String Value) //throws AmazonServiceException
    {

        HashMap<String,AttributeValue> key_to_get =
        new HashMap<String,AttributeValue>();
    
        key_to_get.put(PrimaryKey, new AttributeValue(Value));
        
        GetItemRequest request = null;

        request = new GetItemRequest()
            .withKey(key_to_get)
            .withTableName(TableName);
        
        final AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.defaultClient();
        
        try {
            Map<String,AttributeValue> returned_item =
            ddb.getItem(request).getItem();
            if (returned_item != null) {
                Set<String> keys = returned_item.keySet();
                String output;
                for (String key : keys) {
                    System.out.format("%s: %s\n",
                            key, returned_item.get(key).toString());
                }
                return returned_item.toString();
                
            } else {
                System.out.format("No item found with the key %s!\n", Value);
            }
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            //System.exit(1);
            return(e.getErrorMessage());
        }
        return "";
        
    }

    private String PutItem(String TableName,String PrimaryKey, String PrimaryKeyValue, String Key, String Value) //throws AmazonServiceException,ResourceNotFoundException
    {
        HashMap<String,AttributeValue> item_values =
        new HashMap<String,AttributeValue>();
    
        item_values.put(PrimaryKey, new AttributeValue(PrimaryKeyValue));
        item_values.put(Key, new AttributeValue(Value));
        
        final AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.defaultClient();
        
        try {
            ddb.putItem(TableName, item_values);
        } catch (ResourceNotFoundException e) {
            System.err.format("Error: The table \"%s\" can't be found.\n", TableName);
            System.err.println("Be sure that it exists and that you've typed its name correctly!");
            return e.getMessage();
            //System.exit(1);
            
        } catch (AmazonServiceException e) {
            System.err.println(e.getMessage());
            return e.getMessage();
            //System.exit(1);
        }
        return "";
    }
}
