package com.w3c.prov;

public class Agent {
    private String id;
    private String type;
    private String version;

    public Agent(String id, String type, String version) {
        this.id = id;
        this.type = type;
        this.version = version;
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
        return "\"ex:" + id + '_' + version + "\": { " +
                "\"prov:type\":{" + "\"$\": \"prov:SoftwareAgent\", \"type\": \""+ type +"\"}" + "}";
    }
}