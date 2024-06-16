package com.w3c.prov;

public class Agent {
    private String id;
    private String type;
    private String version;
    private String prompt;
    private String u_prompt;

    public Agent(String id, String type, String version, String prompt, String u_prompt) {
        this.id = id;
        this.type = type;
        this.version = version;
        this.prompt = prompt;
        this.u_prompt = u_prompt;
    }

    public Agent(String id, String type) {
        this.id = id;
        this.type = type;
    }

    public String toStringP() {
        return "{\"prov:id\":" + "\""+ id +"\"," + 
                "\"prov:type\":" + "\""+ type +"\"" +
                "}";
    }
    public String toStringSW() {
        return "{\"prov:id\":" + "\""+ id +"\"," + 
                "\"prov:type\":" + "\""+ type +"\"," + 
                 "\"version\": \""+ version +"\"," +
                 "\"prompt\": \""+ prompt +"\"," +
                 "\"u_prompt\": \""+ u_prompt +"\"" +
                "}";
    }
}