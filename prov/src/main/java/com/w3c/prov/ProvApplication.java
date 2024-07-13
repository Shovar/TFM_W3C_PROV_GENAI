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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
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
            byte[] content= Jwts.parser()
                                .verifyWith(key)
                                .build()
                                .parseSignedContent(jwt)
								.getPayload();
            // Print out the claims
			String str = new String(content);
			byte[] signature = Jwts.parser()
								.verifyWith(key)
								.build()
								.parseSignedContent(jwt)
								.getDigest();
			//String sign = new String(signature);
			String sign = jwt;
			String header = Jwts.parser()
								.verifyWith(key)
								.build()
								.parseSignedContent(jwt)
								.getHeader().toString();
								
			String[] ret = {header, str, sign};
			return ret;
        } catch (SignatureException e) {
            System.out.println("Invalid JWT signature");
        } catch (Exception e) {
            System.out.println("Invalid JWT token");
        }
		return null;
    }

	public static String create_entity(Boolean is_creation, String target, String source) throws Exception {
		String ret_entity;
		Entity entity = new Entity("img/jpeg", target);
		if(is_creation == false){ //Si es una edición, añadir una segunda entidad
			Entity entity2 = new Entity("img/jpeg", source);
			ret_entity = "\"entity\":{" + entity.toString() + "," + entity2.toString() + "}";
		}
		else{
			ret_entity = "\"entity\" :{" + entity.toString() + "}";
		}
		return ret_entity;
	}

	public static String create_activiy(String id, String type) throws Exception{
		return "\"activity\": {\"ex:" + id + "\": { \"prov:type\": \"" +type+ "\"} }";
	}

	public static String create_agent(String model, String version, String prompt, String u_prompt) throws Exception{
		String username = System.getProperty("user.name");
		Agent agent = new Agent( model ,"https://cv.iptc.org/newscodes/digitalsourcetype/trainedAlgorithmicMedia", version, prompt, u_prompt);
		Agent person = new Agent(username);
		String ret_agent = "\"agent\":{" + agent.toStringSW() + "," + person.toStringP() + "}";
		return ret_agent ;
	}

	public static String create_WAIGB(String id, String targetName, String activity, String formattedDate, String model ,String version, String prompt, String u_prompt){
		return "\"_:"+ id +"\":{ \"prov:entity\": \"ex:"+ targetName +"\" ,  \"prov:activity\": \"ex:"+ activity +"\", \"provai:model\": \""+ model + "\" , \"provai:version\": \""+ version + "\", \"provai:prompt\": \""+ prompt +"\", \"provai:u_prompt\": \""+ u_prompt +"\" , \"prov:Time\": \""+ formattedDate + "\"}";
	}

	public static String create_WAW(String id, String targetName, String activity, String agent, String role){
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
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
		String formattedDate = sdf.format(date);
		String wasGeneratedBy = new String();
		String wasDerivedFrom = new String();
		String wasUsedBy = new String();
		String wasAssociatedWith = new String();
		String targetUrl = new String();
		String targetName = new String();
		String content = new String();
		String activity = new String();
		String prefix = " \"prefix\": { \"xsd\": \"http://www.w3.org/2001/XMLSchema#\", \"ex\": \"http://example.com/\", \"prov\": \"http://www.w3.org/ns/prov#\", \"provai\": \"http://example.com/provgenai#\" }";
		if (is_creation) {
			targetUrl = assetFileUrl + "-created";
			targetName = assetName + "-created";
			activity = create_activiy("a1", "Creation");		
			wasAssociatedWith = "\"wasAssociatedWith\": {" + create_WAW("wAW1", targetName, "a1", model, "Image Generator") + "}";
				
		}else{
			targetUrl = assetFileUrl + "-edited";
			targetName = assetName + "-edited";
			activity = create_activiy("a1", "Edition");
			wasUsedBy = "\"used\": { "+ create_WU("u1", "a1", targetName, formattedDate) + "}";		
			wasDerivedFrom = "\"wasDerivedFrom\": { "+ create_WDF("wDF1", targetName, assetName, "Edition") + "}";
			wasAssociatedWith = "\"wasAssociatedWith\": {" + create_WAW("wAW1", targetName, "a1", model, "Image Editor") +"}";
		}
		wasGeneratedBy = "\"wasGeneratedBy\": { "+ create_WAIGB("wGB1", targetName, "a1", formattedDate, model, version, prompt, u_prompt)+"}";	
		String username = System.getProperty("user.name");
		String actedOnBehalfOf = "\"actedOnBehalfOf\": {"+ create_WABO("aOBO1", "a1", model, username) +"}";
		String wasAttributedTo = "\"wasAttributedTo\": {"+ create_WAT("wAT1", targetName, model, "authorship") + "}"; 
		String entity = create_entity(is_creation, targetName , assetName);
		String agent = create_agent(model, version, prompt, u_prompt);
		// Create the content string
		if (is_creation) {
			content = '{' + prefix + "," + entity + "," + activity +  "," + agent + ","  + actedOnBehalfOf + "," + wasGeneratedBy + "," + wasAssociatedWith + "," + wasAttributedTo + '}';	
		}
		else{
			content = '{' + prefix + "," + entity + "," + activity +  "," + agent + "," + actedOnBehalfOf + "," + wasUsedBy + "," + wasGeneratedBy + "," + wasDerivedFrom + "," + wasAssociatedWith + "," + wasAttributedTo + '}'; ;
		}
	
		// Create a JsonBox object
		JsonBox jsonBox = new JsonBox();
		JsonContentType jsonContentType = new JsonContentType();

		String[] decoded_jwt = decode_and_validate_jwt(getJwt(content));
		jsonBox.setContent(("{\"header\": " + decoded_jwt[0] + ", \"payload\": " + decoded_jwt[1] + ", \"signature\": " + decoded_jwt[2] + "}").getBytes());
		jsonBox.updateFieldsBasedOnExistingData();
		// Create a JumbfBoxBuilder object
		JumbfBoxBuilder builder = new JumbfBoxBuilder(jsonContentType);		
		builder.setLabel("W3C_PROV");
		builder.setPaddingSize(10);
		builder.appendContentBox(jsonBox);
		JumbfBox givenJumbfBox = builder.getResult();
		
		
		jpegCodestreamGenerator.generateJumbfMetadataToFile(List.of(givenJumbfBox), assetFileUrl, targetUrl);
		List<JumbfBox> resultList = jpegCodestreamParser.parseMetadataFromFile(targetUrl);
		assertEquals(1, resultList.size());

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

	// entidad prompt, u_prompt
	// agente persona 
	// persona delega a agente software
	// actividad persona writes prompt y u_prompt
	// entidad prompt y u_prompt fue generada por la actividad
	// entidad prompt y u_prompt fue atribuida a la persona
	// prompt y u_prompt fueron usuados por la actividad de crear/editar