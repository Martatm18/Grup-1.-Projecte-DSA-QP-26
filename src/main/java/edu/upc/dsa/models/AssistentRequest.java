package edu.upc.dsa.models;

public class AssistentRequest {
    private String question;

    public AssistentRequest() {
    }

    public AssistentRequest(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
