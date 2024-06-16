package com.w3c.prov;


import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import java.util.Base64;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;

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

import javax.crypto.SecretKey;
import javax.swing.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;



@Import(JumbfConfig.class)
@ComponentScan("org.mipams.jumbf.config")
public class ProvApplication {
	static CoreParserService coreParserService;
	static CoreGeneratorService coreGeneratorService;
	static JpegCodestreamGenerator jpegCodestreamGenerator;
	static JpegCodestreamParser jpegCodestreamParser;
    static JumbfBoxService jumbfBoxService;
	static String assetFileUrl;

	public static String getJwt() {
		SecretKey secretKey = generalKey();
		Claims claims = Jwts.claims().subject("Toni Garcia").build();
		Date date = new Date();
		return Jwts.builder().id(assetFileUrl)
				.subject("W3C_Prov_for_GENAI")
				.claims(claims)
				.issuer("Toni Garcia")
				.issuedAt(date)
				.signWith(secretKey)
				.compact();
		}
	
	public static SecretKey generalKey(){
		byte[] encodeKey = Base64.getDecoder().decode("U2FtcGxlIHBhc3N3b3JkIGZvciB3YzMgcHJvdiBkZXRlY3R5b25nIEdlbiBBSSBpbWFnZXM=");
		return Keys.hmacShaKeyFor(encodeKey);
	}

	public static String create_entity(Boolean is_creation, String target, String source) throws Exception {
		String ret_entity;
		Entity entity = new Entity("https://cv.iptc.org/newscodes/digitalsourcetype/trainedAlgorithmicMedia", target);
		if(is_creation == false){ //Si es una edición, añadir una segunda entidad
			Entity entity2 = new Entity("img/jpeg", source);
			ret_entity = "\"prov:entity\":[" + entity.toString() + "," + entity2.toString() + "]";
		}
		else{
			ret_entity = "\"prov:entity\" :" + entity.toString() ;
		}
		return ret_entity;
	}

	public static String create_activiy(String id, String type) throws Exception{
		return "\"prov:activity\": {\"prov:id\": \""+ id +"\", \"prov:type\": \"" +type+ "\"}";
	}

	public static String create_agent(String model, String version, String prompt, String u_prompt) throws Exception{
		String username = System.getProperty("user.name");
		Agent agent = new Agent( model ,"SoftwareAgent", version, prompt, u_prompt);
		Agent person = new Agent(username ,"Person");
		String ret_agent = "\"prov:agent\":[" + agent.toStringSW() + "," + person.toStringP() + "]";
		return ret_agent ;
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
		
		if (is_creation) {
			targetUrl = assetFileUrl + "-created";
			targetName = assetName + "-created";
			activity = create_activiy("a1", "Creation");
			wasGeneratedBy = "\"prov:wasGeneratedBy\": {\"prov:entity\": \""+ targetName +"\" ,  \"prov:activity\": \"a1\" , \"prov:time\": \""+ formattedDate + "\"}";			
			wasAssociatedWith = "\"prov:wasAssociatedWith\": {\"prov:activity\": \"a1\", \"prov:agent\": \"ag1\", \"prov:role\": \"Image Generator\"}";
				
		}else{
			targetUrl = assetFileUrl + "-edited";
			targetName = assetName + "-edited";
			activity = create_activiy("a1", "Edition");
			wasUsedBy = "\"prov:used\": {\"prov:activity\": \"a1\" , \"prov:entity\": \""+ targetName  +"\" , \"prov:time\": \""+ formattedDate + "\"}";
			wasGeneratedBy = "\"prov:wasGeneratedBy\": {\"prov:entity\": \""+ targetName  +"\" ,  \"prov:activity\": \"a1\" , \"prov:time\": \""+ formattedDate + "\"}";			
			wasDerivedFrom = "\"prov:wasDerivedFrom\": {\"prov:generatedEntity\": \""+ targetName  +"\", \"prov:usedEntity\": \""+ assetName +"\", \"prov:type\": \"Edition\"}";
			wasAssociatedWith = "\"prov:wasAssociatedWith\": {\"prov:activity\": \"a1\", \"prov:agent\": \"ag1\", \"prov:role\": \"Image Editor\"}";
		}
		String entity = create_entity(is_creation, targetName , assetName);
		String agent = create_agent(model, version, prompt, u_prompt);
		String username = System.getProperty("user.name");
		String actedOnBehalfOf = "\"prov:actedOnBehalfOf\": {\"prov:delegate\": \"ag1\", \"prov:responsible\": \""+ username +"\"}";
		String wasAttributedTo = "\"prov:wasAttributedTo\": {\"prov:entity\": \""+ targetName  + "\", \"prov:agent\": \"ag1\", \"prov:type\": \"authorship\"}"; 
		
		// Create the content string
		if (is_creation) {
			content = "{\"document\": {" + entity + "," + activity +  "," + agent + "," + actedOnBehalfOf + "," + wasGeneratedBy + "," + wasAssociatedWith + "," + wasAttributedTo +"}}";	
		}
		else{
			content = "{\"document\": {" + entity + "," + activity +  "," + agent + "," + actedOnBehalfOf + "," + wasUsedBy + "," + wasGeneratedBy + "," + wasDerivedFrom + "," + wasAssociatedWith + "," + wasAttributedTo +"}}";
		}
	
		// Create a JsonBox object
		JsonBox jsonBox = new JsonBox();
		JsonContentType jsonContentType = new JsonContentType();
		jsonBox.setContent(content.getBytes());
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