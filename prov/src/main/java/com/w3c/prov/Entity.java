package com.w3c.prov;

public class Entity {

    private String type;
    private String id;
    private String prompt;


    public Entity( String type, String id) {
        this.type = type;
        this.id = id;
    }

    public Entity(String type, String id, String prompt) {
        this.type = type;
        this.id = id;
        this.prompt = prompt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        if (prompt != null) {
            return "\"ex:"+ id +"\": { \"prov:type\": \""+ type +"\", \"prov:value\": \""+ prompt +"\"}";
        }
        return "\"ex:"+ id +"\": { \"prov:type\": \""+ type +"\"" + "}";
    }

}
