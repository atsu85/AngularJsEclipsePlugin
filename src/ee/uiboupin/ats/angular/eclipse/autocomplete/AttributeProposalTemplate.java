package ee.uiboupin.ats.angular.eclipse.autocomplete;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

class AttributeProposalTemplate {
  private static final String AFTER_ATTRIBUTE_NAME_QUOTES = "=''";
  private final String attributeName;
  private final String description;
  private String afterAttributeName = AFTER_ATTRIBUTE_NAME_QUOTES;
  private Collection<String> allowedTagNames;
  private List<String> requiredAttributesOr;

  public AttributeProposalTemplate(String attributeName, String description) {
    this.attributeName = attributeName;
    this.description = description;
  }

  public String getAttributeName() {
    return attributeName;
  }

  public String getAfterAttributeName() {
    return afterAttributeName;
  }

  public String getDescription() {
    return description;
  }

  public String getAttributeWithValue() {
    return getAttributeName() + getAfterAttributeName();
  }

  public int getCursorPosition() {
    String attributeWithValue = getAttributeWithValue();
    int cursorPosition = attributeWithValue.length(); // cursor should be in the end of the inserted value
    if (AFTER_ATTRIBUTE_NAME_QUOTES.equals(getAfterAttributeName())) {
      cursorPosition -= 1;
    }
    return cursorPosition;
  }

  public AttributeProposalTemplate setAfterAttributeName(String afterAttributeName) {
    this.afterAttributeName = afterAttributeName;
    return this;
  }

  public AttributeProposalTemplate setAllowedTagNames(String... allowedTagNames) {
    this.allowedTagNames = Arrays.asList(allowedTagNames);
    return this;
  }

  public boolean isAppropriateForTag(String tagName) {
    // TODO requiredAttributesOr
    return allowedTagNames == null || allowedTagNames.contains(tagName.toLowerCase());
  }

  // TODO would be better to implement it with Condition that could be combined with logical AND and OR
  public AttributeProposalTemplate setRequiredAttributesOr(String... requiredAttributesOr) {
    this.requiredAttributesOr = Arrays.asList(requiredAttributesOr);
    return this;
  }

  @Override
  public String toString() {
    return attributeName;
  }

}