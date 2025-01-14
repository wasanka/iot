package com.mufg.pex.messaging.util;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HTML {

    private HTML parent;
    private String name;
    private String value;
    private List<HTML> children = new ArrayList<>();
    private Map<String, String> attributes = new HashMap<>();

    public static HTML builder(String root) {

        HTML e = new HTML();
        e.name = root;

        return e;
    }

    public HTML element(String name) {

        return element(name, null);
    }

    public HTML up() {

        return this.parent;
    }

    public HTML attribute(String name, String value){

        attributes.put(name, value);

        return this;
    }

    public HTML element(String name, String value) {

        HTML e = new HTML();
        e.name = name;
        e.value = value;

        this.children.add(e);
        e.parent = this;

        return e;
    }

    public String build() {

        StringBuilder builder = new StringBuilder();
        builder.append("<");
        builder.append(name);

        if (!attributes.isEmpty()) {

            attributes.forEach((s, s2) -> {
                builder.append(" ");
                builder.append(s);
                builder.append("=\"");
                builder.append(s2);
                builder.append("\"");
            });
        }

        builder.append(">");

        if (children.isEmpty()) {
            if (value != null) {
                builder.append(value);
            }
        } else {
            children.forEach(element -> {

                builder.append(element.build());
                builder.append("\n");
            });
        }

        builder.append("</");
        builder.append(name);
        builder.append(">");

        return builder.toString();
    }
}