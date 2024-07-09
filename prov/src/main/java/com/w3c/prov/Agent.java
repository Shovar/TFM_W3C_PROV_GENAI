package com.w3c.prov;

public class Agent {
    private String id;
    private String type;
    /*private String version;
    private String prompt;
    private String u_prompt;*/

    public Agent(String id, String type, String version, String prompt, String u_prompt) {
        this.id = id;
        this.type = type;
       /* this.version = version;
        this.prompt = prompt;
        this.u_prompt = u_prompt;*/
    }

    public Agent(String id) {
        this.id = id;
    }

    public String toStringP() {
        return "\"ex:" + id + "\": { " + 
                 "\"prov:type\":{" + "\"$\": \"prov:Person\", \"type\": \"xsd:QName\"}" +
                "}";
    }
    public String toStringSW() {
        return "\"ex:" + id + "\": { " +
                "\"prov:type\":{" + "\"$\": \"prov:SoftwareAgent\", \"type\": \""+ type +"\"}" + "}";
    }
}