package edu.upc.dsa.models;

public class AssistentRequest {
    private String question;
    private String context;

    public AssistentRequest() {
    }

    public AssistentRequest(String question) {
        this.question = question;
    }

    public AssistentRequest(String question, String context) {
        this.question = question;
        this.context = context;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
