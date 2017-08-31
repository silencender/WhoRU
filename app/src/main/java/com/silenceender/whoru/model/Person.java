package com.silenceender.whoru.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import static com.silenceender.whoru.utils.ToolHelper.*;

/**
 * Created by Silen on 2017/8/20.
 */

public class Person {
    private String name;
    private List<String> picname = new ArrayList<String>();

    public Person(String name) {
        this.name = name;
    }

    public Person(String name,String picname) {
        this.name = name;
        this.picname = stringToList(picname);
    }

    public void addPicname(String picname) {
        if(picname != "") {
            this.picname.add(picname);
        }
        for(int i=0;i<this.picname.size();i++) {
            if(this.picname.get(i) == "") {
                this.picname.remove(i);
            }
        }
    }

    public void removePicname(int i) {
        this.picname.remove(i);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPicnames(String picname) {
        this.picname = stringToList(picname);
    }

    public void setPicnames(List<String> picname) {
        this.picname = picname;
    }

    public String getName() {
        return this.name;
    }

    public String getPicnames() {
        return stringJoin(this.picname);
    }

    public List<String> getPicnameList() {
        return this.picname;
    }

    public String getJSONEncode() {
        JSONObject jsonPerson=new JSONObject();
        try{
            jsonPerson.put("name", this.getName());
            jsonPerson.put("picname",this.getPicnames());
            return jsonPerson.toString();
        }catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }
}
