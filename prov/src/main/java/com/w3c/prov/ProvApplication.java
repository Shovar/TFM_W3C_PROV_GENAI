package com.w3c.prov;


import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import javax.swing.*;

import org.springframework.util.ResourceUtils;
import org.mipams.jumbf.entities.JsonBox;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.entities.JumbfBoxBuilder;
import org.mipams.jumbf.services.CoreGeneratorService;
import org.mipams.jumbf.services.CoreParserService;
import org.mipams.jumbf.services.boxes.JumbfBoxService;
import org.mipams.jumbf.services.content_types.JsonContentType;
import org.mipams.jumbf.config.JumbfConfig;
import org.mipams.jumbf.services.JpegCodestreamGenerator;
import org.mipams.jumbf.services.JpegCodestreamParser;





@Import(JumbfConfig.class)
@ComponentScan("org.mipams.jumbf.config")
public class ProvApplication {
	static CoreParserService coreParserService;
	static CoreGeneratorService coreGeneratorService;
	static JpegCodestreamGenerator jpegCodestreamGenerator;
	static JpegCodestreamParser jpegCodestreamParser;
    static JumbfBoxService jumbfBoxService;
	static String assetFileUrl;
	static String JWT_KEY = "8963SZ7KKZlDb8OzfHJDKXvHtyNjklMZeZcZYxXb0mxHGPvu3sKKkBBuP7WsATf1srPgt1Oprg77XS6PYyMo6sqgcaSoxUthnRf";

	public static String getJwt(String content) throws Exception{
		
		JwtBuilder myBuilder = Jwts.builder();
		//Header
		myBuilder.header().keyId("1")
						.contentType("text/json")
						.and();

		//Payloads - Start
		Map<String,Object> myContent = new HashMap<>();
		myContent.put("userId","1");
		myContent.put("sessionId","1");
		myBuilder.content(content, "text/json");
		//Payloads - End

		//Signature
		Key mySigningKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(JWT_KEY));
		myBuilder.signWith(mySigningKey);

		String jwtToken = myBuilder.compact();
		return jwtToken;
	}

	 public static String[] decode_and_validate_jwt(String jwt) {

        // Decode and validate the JWT
        try {
            // Convert the secret to a Key object
			SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(JWT_KEY));

            // Parse the JWT
			String header = Jwts.parser()
							.verifyWith(key)
							.build()
							.parseSignedContent(jwt)
							.getHeader().toString();
			//Parse header to json format
			header = header.replace("{", "{ \"");
			header = header.replace("}", "\"}");
			header = header.replace("=", "\":\"");
			header = header.replace(",", "\", \"");
			header = header.replaceAll("\\s+", "");
			

            byte[] content= Jwts.parser()
                                .verifyWith(key)
                                .build()
                                .parseSignedContent(jwt)
								.getPayload();
            // Print out the claims
			String str = new String(content);
			String signature = Jwts.parser()
								.verifyWith(key)
								.build()
								.parseSignedContent(jwt)
								.getSignature();
			java.util.Base64.Decoder decoder = java.util.Base64.getUrlDecoder();
			String Signature = decoder.decode(signature).toString();

			String sign = Signature;

			String[] ret = {header, str, sign};
			return ret;
        } catch (SignatureException e) {
            System.out.println("Invalid JWT signature");
        } catch (Exception e) {
            System.out.println("Invalid JWT token");
        }
		return null;
    }

	public static String create_entity_img(Boolean is_creation, String target, String source) throws Exception {
		String ret_entity;
		if(is_creation == false){ //Si es una edición, añadir una segunda entidad
			Entity entity = new Entity("https://cv.iptc.org/newscodes/digitalsourcetype/compositeWithTrainedAlgorithmicMedia", target);
			Entity entity2 = new Entity("https://cv.iptc.org/newscodes/digitalsourcetype/trainedAlgorithmicMedia", source);
			ret_entity =  entity.toString() + "," + entity2.toString();
		}
		else{
			Entity entity = new Entity("https://cv.iptc.org/newscodes/digitalsourcetype/trainedAlgorithmicMedia", target);
			ret_entity =  entity.toString() ;
		}
		return ret_entity;
	}

	public static String create_entity_prompt( String id, String prompt) throws Exception {
		String ret_entity;
			Entity entity = new Entity("String", id, prompt);
			ret_entity =  entity.toString();
		return ret_entity;
	}

	public static String create_activiy(String id, String type) throws Exception{
		return "\"ex:" + id + "\": { \"prov:type\": \"" +type+ "\"}";
	}

	public static String create_agent(String model, String version, String prompt, String u_prompt, Boolean is_creation) throws Exception{
		String username = System.getProperty("user.name");
		Agent agent = new Agent( model ,"Generative AI Model", version);
		Agent person = new Agent(username);
		String ret_agent = "\"agent\":{" + agent.toStringSW() + "," + person.toStringP() + "}";
		return ret_agent ;
	}

	public static String create_WAIGB(String id, String targetName, String activity, String formattedDate){
		return "\"_:"+ id +"\":{ \"prov:entity\": \"ex:"+ targetName +"\" ,  \"prov:activity\": \"ex:"+ activity +"\", \"prov:Time\": \""+ formattedDate + "\"}";
	}

	public static String create_WAW(String id, String activity, String agent, String role){
		return "\"_:"+ id +"\":{ \"prov:activity\": \"ex:"+ activity +"\", \"prov:agent\": \"ex:"+ agent +"\", \"prov:role\": \""+ role +"\"}";
	}

	public static String create_WDF(String id, String generatedEntity, String usedEntity, String type){
		return "\"_:"+ id +"\":{ \"prov:generatedEntity\": \"ex:"+ generatedEntity +"\", \"prov:usedEntity\": \"ex:"+ usedEntity +"\", \"prov:type\": \""+ type +"\"}";
	}

	public static String create_WAT(String id, String entity, String agent, String type){
		return "\"_:"+ id +"\":{ \"prov:entity\": \"ex:"+ entity +"\", \"prov:agent\": \"ex:"+ agent +"\", \"prov:type\": \""+ type +"\"}";
	}

	public static String create_WU(String id, String activity, String entity, String formattedDate){
		return "\"_:"+ id +"\":{ \"prov:activity\": \"ex:"+ activity +"\", \"prov:entity\": \"ex:"+ entity +"\", \"prov:Time\": \""+ formattedDate +"\"}";
	}

	public static String create_WABO(String id, String activity, String agent1, String agent2){
		return "\"_:"+ id +"\":{ \"prov:activity\": \"ex:"+ activity +"\", \"prov:delegate\": \"ex:"+ agent1 +"\", \"prov:responsible\": \"ex:"+ agent2 +"\"}";
	}

	public static void create_prov(Boolean is_creation, String assetName ,String model, String version, String prompt, String u_prompt) throws Exception{
		// Variable initialization
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
		String formattedDate = sdf.format(date);
		String wasDerivedFrom = new String();
		String wasUsedBy = new String();
		String wasAssociatedWith = new String();
		String targetUrl = new String();
		String targetName = new String();
		String content = new String();
		String model_activity = new String();
		String username = System.getProperty("user.name");
		if (is_creation) {
			// Creation specific variables
			targetUrl = assetFileUrl + "-created";
			targetName = assetName + "-created";
			//Creation activity
			model_activity = create_activiy("a1", "Creation");	
			//Creation Relations
			wasUsedBy = "\"used\": { "+ create_WU("u1", "a1", "Prompt", formattedDate) + "," 
									  + create_WU("u2", "a1", "Uncond_prompt", formattedDate) +"}";
			wasAssociatedWith = "\"wasAssociatedWith\": {" + create_WAW("wAW1", "a1", model, "Image Generator") + ',' 
														   + create_WAW("wAW2",  "a2", username, "Prompt Writer") +  "}";
				
		}else{
			// Edition specific variables
			targetUrl = assetFileUrl + "-edited";
			targetName = assetName + "-edited";
			//Edition activity
			model_activity = create_activiy("a1", "Edition");
			//Edition Relations
			wasUsedBy = "\"used\": { "+ create_WU("u1", "a1", assetName, formattedDate) + "," 
									  + create_WU("u2", "a1", "Prompt", formattedDate) + "," 
									  + create_WU("u3", "a1", "Uncond_prompt", formattedDate) + "}";		
			wasDerivedFrom = "\"wasDerivedFrom\": { "+ create_WDF("wDF1", targetName, assetName, "Edition") + "}";
			wasAssociatedWith = "\"wasAssociatedWith\": {" + create_WAW("wAW1", "a1", model, "Image Editor") + ',' 
														   + create_WAW("wAW2",  "a2", username, "Prompt Writer") + "}";
		}
		// Prefix
		String prefix = " \"prefix\": { \"xsd\": \"http://www.w3.org/2001/XMLSchema#\", \"ex\": \"http://example.com/\", \"prov\": \"http://www.w3.org/ns/prov#\" }";
		//Entities
		String entity_img  = create_entity_img(is_creation, targetName , assetName);
		String promp_ent   = create_entity_prompt( "Prompt", prompt);
		String u_promp_ent = create_entity_prompt( "Uncond_prompt", u_prompt);
		String entity = "\"entity\" :{" + entity_img + "," + promp_ent + "," + u_promp_ent + "}";
		//Agents
		String agent = create_agent(model, version, prompt, u_prompt, is_creation);
		//Activities
		String write_activity = create_activiy("a2", "Write");
		String activity = "\"activity\": {" + model_activity + "," + write_activity + "}";
		//Relations
		String wasGeneratedBy = "\"wasGeneratedBy\": { "+ create_WAIGB("wGB1", targetName, "a1", formattedDate) + ',' 
												        + create_WAIGB("wGB2", "Prompt", "a2", formattedDate) + ',' 
												        + create_WAIGB("wGB3", "Uncond_prompt", "a2", formattedDate) + "}";	
		
		String actedOnBehalfOf = "\"actedOnBehalfOf\": {"+ create_WABO("aOBO1", "a1", model, username) +"}";
		String wasAttributedTo = "\"wasAttributedTo\": {"+ create_WAT("wAT1", targetName, model, "authorship") + ',' 
														 + create_WAT("wAT2", "Prompt", username, "authorship") + ',' 
														 + create_WAT("wAT3", "Uncond_prompt", username, "authorship") + "}"; 
		// Create the content string
		if (is_creation) {
			content = '{' + prefix + "," + entity + "," + activity +  "," + agent + ","  + actedOnBehalfOf + "," + wasUsedBy + "," + wasGeneratedBy + "," + wasAssociatedWith + "," + wasAttributedTo + '}';	
		}
		else{
			content = '{' + prefix + "," + entity + "," + activity +  "," + agent + "," + actedOnBehalfOf + "," + wasUsedBy + "," + wasGeneratedBy + "," + wasDerivedFrom + "," + wasAssociatedWith + "," + wasAttributedTo + '}'; ;
		}
	
		// Create a JsonBox object
		JsonBox jsonBox = new JsonBox();
		JsonContentType jsonContentType = new JsonContentType();
        // Decode and validate the JWT
		String[] decoded_jwt = decode_and_validate_jwt(getJwt(content));
		// Set the content of the JsonBox object
		jsonBox.setContent(("{\"header\": " + decoded_jwt[0] + ", \"payload\": " + decoded_jwt[1] + ", \"signature\": \"" + decoded_jwt[2] + "\"}").getBytes());
		jsonBox.updateFieldsBasedOnExistingData();
		// Create a JumbfBoxBuilder object
		JumbfBoxBuilder builder = new JumbfBoxBuilder(jsonContentType);		
		builder.setLabel("W3C_PROV");
		builder.setPaddingSize(10);
		builder.appendContentBox(jsonBox);
		JumbfBox givenJumbfBox = builder.getResult();
		// Generate the Jumbf metadata
		jpegCodestreamGenerator.generateJumbfMetadataToFile(List.of(givenJumbfBox), assetFileUrl, targetUrl);
		List<JumbfBox> resultList = jpegCodestreamParser.parseMetadataFromFile(targetUrl);
		assertEquals(1, resultList.size());

		System.out.println("{\"header\": " + decoded_jwt[0] + ", \"payload\": " + decoded_jwt[1] + ", \"signature\": \"" + decoded_jwt[2] + "\"}");
	}

	public static void main(String[] args ) throws Exception {
		
		@SuppressWarnings("resource")
		ApplicationContext context = new AnnotationConfigApplicationContext(JumbfConfig.class);
		jpegCodestreamParser= context.getBean(JpegCodestreamParser.class);
		jpegCodestreamGenerator = context.getBean(JpegCodestreamGenerator.class);
		jumbfBoxService = context.getBean(JumbfBoxService.class);

		//JFrame
		JFrame frame = new JFrame();
		//Radio buttons
		JRadioButton r1=new JRadioButton("A) Create");    
		JRadioButton r2=new JRadioButton("B) Edit");    
		r1.setBounds(50,50,100,30);    
		r2.setBounds(150,50,100,30);   
		ButtonGroup bg=new ButtonGroup();    
		bg.add(r1);bg.add(r2);    
		frame.add(r1);frame.add(r2);  
		//labels
		JLabel l1,l2,l3, l4, l5, l6;
		l1=new JLabel("Source File:");  
		l1.setBounds(50,100, 100,30);  
		l2=new JLabel("AI Model:");  
		l2.setBounds(50,125, 100,30); 
		l3=new JLabel("Version:");
		l3.setBounds(50,150, 100,30); 
		l4=new JLabel("Prompt:");
		l4.setBounds(25,220, 100,30);
		l5=new JLabel("Negative Prompt:");
		l5.setBounds(25,370, 100,30);
		l6=new JLabel();
		l6.setBounds(10,600, 200,30);
		frame.add(l1); frame.add(l2);frame.add(l3);frame.add(l4); frame.add(l5); frame.add(l6);
		//Text fields
		JTextField t1,t2,t3;  
		t1=new JTextField("sample.jpeg");  
		t1.setBounds(155,100, 250,30);  
		t2=new JTextField("Stable Diffusion");  
		t2.setBounds(155,125, 250,30);  
		t3=new JTextField("Turbo");  
		t3.setBounds(155,150, 250,30);  
		frame.add(t1); frame.add(t2); frame.add(t3); 
		//Text area
		JTextArea area1, area2;
		area1=new JTextArea();
		area1.setBounds(10,250, 550,100);  
		area2=new JTextArea();
		area2.setBounds(10,400, 550,100);  
		frame.add(area1);  frame.add(area2); 
		//Execute button
		JButton button = new JButton("Execute");       
		button.setBounds(240, 550, 120, 30);
		
		button.addActionListener(e -> {
			try {
				try {
					assetFileUrl = ResourceUtils.getFile("classpath:" + t1.getText() ).getAbsolutePath();
				} catch (FileNotFoundException asset_e) {
					System.out.println("Error reading source file");
					l6.setText("Error reading source file. No such file in target directory.");
					asset_e.printStackTrace();
					return;
				}

				if(r1.isSelected()){
					create_prov(true, t1.getText(), t2.getText(), t3.getText(), area1.getText(), area2.getText() );
					l6.setText("Creation completed");
				}
				else if(r2.isSelected()){
					create_prov(false, t1.getText(), t2.getText(), t3.getText(), area1.getText(), area2.getText() );
					l6.setText("Edition completed");
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
		frame.add(button);
		frame.setSize(600, 700);
		frame.setLayout(null);
		frame.setVisible(true);
	}

}
