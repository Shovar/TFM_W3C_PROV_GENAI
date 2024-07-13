package com.w3c.prov;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class ProvApplicationTests {

	@Test
	void createEntity() throws Exception {
		assertEquals("\"entity\" :{" + "\"ex:sample\": { \"prov:type\": \"img/jpeg\"}}" , ProvApplication.create_entity(true, "sample" , "img/jpeg"));
	}

	@Test
	void createAgent() {
		Agent agent = new Agent("toni", "testType", "1.0", "prompt", "u_prompt");
		assertEquals("\"ex:toni\": { \"prov:type\":{\"$\": \"prov:SoftwareAgent\", \"type\": \"testType\"},\"version\": \"1.0\",\"prompt\": \"prompt\",\"u_prompt\": \"u_prompt\"}", agent.toStringSW());
	}

	@Test
	void createPerson() {
		Agent agent = new Agent("toni");
		assertEquals("\"ex:toni\": { \"prov:type\":{\"$\": \"prov:Person\", \"type\": \"xsd:QName\"}}", agent.toStringP());
	}

	@Test
	void createActivity() throws Exception {
		assertEquals("\"activity\": {\"ex:a1\": { \"prov:type\": \"Creation\"} }", ProvApplication.create_activiy("a1", "Creation"));
	}

	@Test
	void Create_jwt() throws Exception {
		String jwt = ProvApplication.getJwt("toni");
		System.out.println(jwt.toString());
	}

}
