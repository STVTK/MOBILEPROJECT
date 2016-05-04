package com.stv.mynotes.Entity;

public class Note {
    private int id;
    private String title;
    private String text;
    private String createdDate;
    private String editedDate;

    public Note(){

    }
    public Note(int id){
        this.id=id;
    }
    public Note(int id, String content, boolean isTitle){
        this.id=id;
        if(isTitle){
            this.title=content;
        } else{
            this.text=content;
        }
    }
    public Note(int id, String title, String text){
        this.id=id;
        this.title=title;
        this.text=text;
    }
    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getEditedDate() {
        return editedDate;
    }

    public void setEditedDate(String editedDate) {
        this.editedDate = editedDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
