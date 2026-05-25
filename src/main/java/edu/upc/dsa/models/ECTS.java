package edu.upc.dsa.models;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ECTSResponse {
    private String idUser;
    private int ects;

    public ECTSResponse() {}

    public ECTSResponse(String idUser, int ects) {
        this.idUser = idUser;
        this.ects = ects;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public int getEcts() {
        return ects;
    }

    public void setEcts(int ects) {
        this.ects = ects;
    }
}
