package com.fastcampus.minischeduler.scheduleradmin;

import java.beans.PropertyEditorSupport;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeEditor extends PropertyEditorSupport {
    private final DateTimeFormatter formatter;
    public LocalDateTimeEditor(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException{
        setValue(LocalDateTime.parse(text, formatter));
    }
}
