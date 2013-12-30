package ee.uiboupin.ats.angular.eclipse.autocomplete;

/**
 * AngularJs framework specific class for html attribute proposals
 * 
 * @author Ats Uiboupin
 */
class AngularAttributeProposalTemplate extends AttributeProposalTemplate implements Comparable<AngularAttributeProposalTemplate> {

  public AngularAttributeProposalTemplate(String attributeName, String description) {
    super(attributeName, description + " - " + getDirectiveDocumentationUrl(attributeName));
  }

  public AngularAttributeProposalTemplate(String attributeName, String description, AngularAttributeProposalTemplate related) {
    super(attributeName, description + " - " + getDirectiveDocumentationUrl(related.getAttributeName()));
  }

  private static String getDirectiveDocumentationUrl(String attributeName) {
    return "http://docs.angularjs.org/api/ng.directive:" + AngularJsUtil.getDirectiveNameFromHtmlAttribute(attributeName);
  }

  @Override
  public AngularAttributeProposalTemplate setAllowedTagNames(String... allowedTagNames) {
    return (AngularAttributeProposalTemplate) super.setAllowedTagNames(allowedTagNames);
  }

  @Override
  public AngularAttributeProposalTemplate setRequiredAttributesOr(String... requiredAttributesOr) {
    return (AngularAttributeProposalTemplate) super.setRequiredAttributesOr(requiredAttributesOr);
  }

  @Override
  public AngularAttributeProposalTemplate setAfterAttributeName(String afterAttributeName) {
    return (AngularAttributeProposalTemplate) super.setAfterAttributeName(afterAttributeName);
  }

  @Override
  public int compareTo(AngularAttributeProposalTemplate o) {
    return this.getAttributeName().compareTo(o.getAttributeName());
  }

}