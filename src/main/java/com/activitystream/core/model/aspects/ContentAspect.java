package com.activitystream.core.model.aspects;

import com.activitystream.sdk.ASConstants;
import com.activitystream.core.model.interfaces.EnrichableElement;
import com.activitystream.core.model.validation.AdjustedPropertyWarning;
import com.activitystream.core.model.validation.IgnoredPropertyError;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ContentAspect extends AbstractMapAspect implements EnrichableElement {

    public static final AspectType ASPECT_TYPE = new AspectType.Embedded(ASConstants.ASPECTS_CONTENT, ContentAspect::new, AspectType.MergeStrategy.REPLACE);

    public static final Pattern REFERENCE_PATTERN = Pattern.compile("@([a-zA-Z0-9/-]+)");
    private static final Pattern NON_TEXT_PATTERN = Pattern.compile(":\\w+:|" + REFERENCE_PATTERN.pattern());

    public ContentAspect() {
    }

    /************
     * Utility functions
     ************/

    @Override
    public AspectType getAspectType() {
        return ASPECT_TYPE;
    }

    /************
     * CEP Utility Functions and Getters
     ************/

    public String getTitle() {
        return (String) get(ASConstants.FIELD_TITLE);
    }

    public void setTitle(String title) {
        if (title != null && !title.isEmpty()) put(ASConstants.FIELD_TITLE, title);
        else remove(ASConstants.FIELD_TITLE);
    }

    public ContentAspect withTitle(String title) {
        setTitle(title);
        return this;
    }

    public String getSubtitle() {
        return (String) get(ASConstants.FIELD_SUBTITLE);
    }

    public void setSubtitle(String subtitle) {
        if (subtitle != null && !subtitle.isEmpty()) put(ASConstants.FIELD_SUBTITLE, subtitle);
        else remove(ASConstants.FIELD_SUBTITLE);
    }

    public ContentAspect withSubtitle(String subtitle) {
        setSubtitle(subtitle);
        return this;
    }

    public String getByline() {
        return (String) get(ASConstants.FIELD_BYLINE);
    }

    public void setByline(String byline) {
        if (byline != null && !byline.isEmpty()) put(ASConstants.FIELD_BYLINE, byline);
        else remove(ASConstants.FIELD_BYLINE);
    }

    public ContentAspect withByline(String byline) {
        setByline(byline);
        return this;
    }

    public String getContent() {
        return (String) get(ASConstants.FIELD_CONTENT);
    }

    public void setContent(String content) {
        if (content != null && !content.isEmpty()) put(ASConstants.FIELD_CONTENT, content);
        else remove(ASConstants.FIELD_CONTENT);
    }

    public ContentAspect withContent(String content) {
        setContent(content);
        return this;
    }

    /************ Assignment & Validation ************/

    @Override
    public Object put(Object key, Object value) {

        String theKey = key.toString();
        String theLCKey = theKey.toLowerCase();
        if (!theKey.equals(theLCKey)) {
            this.addProblem(new AdjustedPropertyWarning("The property name: '" + theKey + "' was converted to lower case"));
            theKey = theLCKey;
        }

        switch (theKey) {
            case ASConstants.FIELD_SUBJECT:
                theKey = ASConstants.FIELD_TITLE;
            case ASConstants.FIELD_TITLE:
                value = validator().processString(theKey, value, true);
                break;
            case ASConstants.FIELD_SUBTITLE:
            case ASConstants.FIELD_BYLINE:
            case ASConstants.FIELD_CONTENT:
            case ASConstants.FIELD_DETECTED_LANGUAGE: //Locale string
                value = validator().processString(theKey, value, false);
                break;
            case ASConstants.FIELD_SENTIMENT: //<String, double>
                break;
            default:
                if (!theKey.startsWith("_"))
                    this.addProblem(new IgnoredPropertyError("The " + theKey + " property is not supported for the Content Aspect"));
        }

        return super.put(theKey, value);
    }

    protected Object directPut(Object key, Object value) {
        return super.put(key, value);
    }

    @Override
    public void verify() {

    }

    /************  Enrichment ************/

    private String getTotalContent() {
        return Stream.of(getTitle(), getSubtitle(), getByline(), getContent())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .map(s -> s.endsWith(".") ? s : (s + "."))
                .collect(Collectors.joining("\n"));
    }

    public String getTotalContentWithoutMarkup() {
        throw new NotImplementedException();
        /*
        todo - implement here
        Parser markdownParser = Parser.builder().build();
        TextContentRenderer renderer = TextContentRenderer.builder().build();
        String textContent = renderer.render(markdownParser.parse(getTotalContent()));
        return NON_TEXT_PATTERN.matcher(textContent).replaceAll(" ");
        */
    }

    /************  Persistence ************/

    public static ContentAspect content() {
        return new ContentAspect();
    }

}
