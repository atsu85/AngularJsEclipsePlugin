package ee.uiboupin.ats.angular.eclipse.autocomplete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.sse.ui.contentassist.ICompletionProposalComputer;
import org.eclipse.wst.sse.ui.internal.contentassist.ContentAssistUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import ee.uiboupin.ats.angular.eclipse.Activator;

@SuppressWarnings("restriction")
public class AngularJSHtmlCompletionProposalComputer implements ICompletionProposalComputer {

  @Override
  public List<ICompletionProposal> computeContextInformation(CompletionProposalInvocationContext context, IProgressMonitor monitor) {
    return Collections.emptyList();
  }

  @Override
  public String getErrorMessage() {
    return null;
  }

  @Override
  public void sessionEnded() {
  }

  @Override
  public void sessionStarted() {
  }

  @Override
  public List<ICompletionProposal> computeCompletionProposals(CompletionProposalInvocationContext context, IProgressMonitor monitor) {
    List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
    // find out xml node where the context assist was started
    int invocationOffset = context.getInvocationOffset();
    Node selectedNode = (Node) ContentAssistUtils.getNodeAt(context.getViewer(), invocationOffset);

    Element tag = null;
    String tagName = null;
    if (selectedNode instanceof Element) {
      tag = (Element) selectedNode;
      tagName = tag.getTagName();
    } else if (selectedNode instanceof Text && selectedNode.getParentNode() instanceof Element) {
      tag = (Element) selectedNode.getParentNode();
    } else {
      return proposals; // comment or somewhere before/after root element (for example empty line or processing instruction)
    }

    NamedNodeMap attributes = tag.getAttributes();
    Set<AngularAttributeProposalTemplate> attributesForProposing = getAngularBuiltInDirectiveAttributes(attributes);

    // find out what user has typed so far
    String partialAttributeName = tagName == null ? "" : getPartialAttributeName(context.getDocument(), invocationOffset);
    int partialAttributeNameOffset = invocationOffset - partialAttributeName.length();

    if (tagName != null) {
      // only add attribute proposals if content assist was requested from tag (ideally we should also check if it's not closing tag)
      for (AngularAttributeProposalTemplate angularAttributeProposalTemplate : attributesForProposing) {
        if (getRejectReason(tagName, attributes, partialAttributeName, angularAttributeProposalTemplate) != null) {
          continue;
        }
        String replacementString = angularAttributeProposalTemplate.getAttributeWithValue();
        int cursorPosition = angularAttributeProposalTemplate.getCursorPosition();
        proposals.add(new CompletionProposal(
            replacementString, // replacementString - the actual string to be inserted into the document
            partialAttributeNameOffset, // replacementOffset - the offset of the text to be replaced
            partialAttributeName.length(), // replacementLength - the length of the text to be replaced
            cursorPosition, // cursorPosition - the position of the cursor following the insert relative to replacementOffset
            Activator.getImageDescriptor("icons/angularjs-16.png").createImage(),
            null,
            null,
            angularAttributeProposalTemplate.getDescription()
            ));
      }
    }
    proposals.add(createCompletionProposalForAngularExpression(invocationOffset));

    return proposals;
  }

  private String getRejectReason(String tagName, NamedNodeMap attributes, String partialAttributeName, AngularAttributeProposalTemplate angularAttributeProposalTemplate) {
    String cantUseReason = null;
    String attributeName = angularAttributeProposalTemplate.getAttributeName();
    if (!attributeName.startsWith(partialAttributeName)) {
      cantUseReason = angularAttributeProposalTemplate + " doesn't start with '" + partialAttributeName + "'";
    }
    if (!angularAttributeProposalTemplate.isAppropriateForTag(tagName)) {
      cantUseReason = angularAttributeProposalTemplate + " can't be used on tag '" + tagName + "'";
    }
    if (attributes.getNamedItem(attributeName) != null) {
      cantUseReason = angularAttributeProposalTemplate + " is already added to the tag '" + tagName + "'";
    }
    return cantUseReason;
  }

  /**
   * @param document
   * - document where content assist is requested
   * @param invocationOffset
   * - position of the cursor when content assist was invoked
   * @return string between cursor and last whitespace before cursor
   */
  private String getPartialAttributeName(IDocument document, int invocationOffset) {
    String partialAttributeName = "";
    // handle situation where user has started typing attribute that we might want to suggest
    try {
      // scan for last whitespace before cursor position
      int beforeLength = 30; // should be long enough
      int begin = invocationOffset - beforeLength;
      if (begin < 0) {
        // we are very close to the beginning of the document
        begin = 0;
        beforeLength = invocationOffset;
      }
      String string = document.get(begin, beforeLength);
      Integer lastWhiteSpaceIndex = null;
      for (int i = string.length() - 1; i > 0; i--) {
        if (Character.isWhitespace(string.charAt(i))) {
          lastWhiteSpaceIndex = i + 1;
          break;
        }
      }
      begin += lastWhiteSpaceIndex;
      partialAttributeName = document.get(begin, invocationOffset - begin);
    } catch (BadLocationException e) {
      throw new RuntimeException(e);
    }
    return partialAttributeName;
  }

  private CompletionProposal createCompletionProposalForAngularExpression(int invocationOffset) {
    return new CompletionProposal(
        "{{  }}", // replacementString - the actual string to be inserted into the document
        invocationOffset, // replacementOffset - the offset of the text to be replaced
        0, // replacementLength - the length of the text to be replaced
        3, // cursorPosition - the position of the cursor following the insert relative to replacementOffset
        Activator.getImageDescriptor("icons/angularjs-16-black.png").createImage(),
        null,
        null,
        "angular expression - http://docs.angularjs.org/guide/expression");
  }

  private void addAttributesIfMissing(NamedNodeMap attributes, Collection<AngularAttributeProposalTemplate> attributeNamesForProposing,
      Collection<AngularAttributeProposalTemplate> attributeNamesToAdd) {
    for (AngularAttributeProposalTemplate angularAttributeProposalTemplate : attributeNamesToAdd) {
      String attributeName = angularAttributeProposalTemplate.getAttributeName();
      if (attributes.getNamedItem(attributeName) == null) {
        attributeNamesForProposing.add(angularAttributeProposalTemplate);
      }
    }
  }

  private Set<AngularAttributeProposalTemplate> getAngularBuiltInDirectiveAttributes(NamedNodeMap attributes) {
    AngularAttributeProposalTemplate ngPluralize = new AngularAttributeProposalTemplate("ng-pluralize", "displays messages according to en-US localization rules");
    AngularAttributeProposalTemplate ngRepeat = new AngularAttributeProposalTemplate(
        "ng-repeat",
        "instantiates a template once per item from a collection. Each template instance gets its own scope, where the given loop variable is set to the current collection item, and $index is set to the item index or key.");
    // TODO add ng-repeat loop variables: $index, $first, $middle, $last, $even, $odd (probably should be available between "{{" and "}}")
    // TODO add ng-repeat-start and ng-repeat-end as well
    AngularAttributeProposalTemplate ngSwitch = new AngularAttributeProposalTemplate("ng-switch",
        "The ngSwitch directive is used to conditionally swap DOM structure on your template based on a scope expression. "
            + "Elements within ngSwitch but without ngSwitchWhen or ngSwitchDefault directives will be preserved at the location as specified in the template.");
    List<AngularAttributeProposalTemplate> angularBuiltInDirectiveAttributes = Arrays
        .asList(
            new AngularAttributeProposalTemplate("ng-app", "designates the root element of the application and is typically placed near the root element of the page"),
            new AngularAttributeProposalTemplate(
                "ng-bind",
                "tells Angular to replace the text content of the specified HTML element with the value of a given expression, and to update the text content when the value of that expression changes"),
            new AngularAttributeProposalTemplate("ng-bind-html",
                "Creates a binding that will innerHTML the result of evaluating the expression into the current element in a secure way"),
            new AngularAttributeProposalTemplate("ng-bind-template",
                "specifies that the element text content should be replaced with the interpolation of the template in the ngBindTemplate attribute. "
                    + "Unlike ngBind, the ngBindTemplate can contain multiple {{ }} expressions."),
            new AngularAttributeProposalTemplate("ng-blur", "specify custom behavior on blur event")
                .setAllowedTagNames("window", "input", "select", "textarea", "a"),
            new AngularAttributeProposalTemplate("ng-change",
                "Evaluate given expression when user changes the input. The expression is not evaluated when the value change is coming from the model")
                .setAllowedTagNames("input"),
            new AngularAttributeProposalTemplate("ng-checked", "Binding expression for input[checked]")
                .setAllowedTagNames("input")
                .setAllowedTagNames("input"),
            new AngularAttributeProposalTemplate("ng-class", "allows you to dynamically set CSS classes on an HTML element"),
            new AngularAttributeProposalTemplate("ng-class-even",
                "The ngClassOdd and ngClassEven directives work exactly as ngClass, except they work in conjunction with ngRepeat and take effect only on odd (even) rows"),
            new AngularAttributeProposalTemplate("ng-class-odd",
                "The ngClassOdd and ngClassEven directives work exactly as ngClass, except they work in conjunction with ngRepeat and take effect only on odd (even) rows"),
            new AngularAttributeProposalTemplate("ng-click", "custom behavior when an element is clicked"),
            new AngularAttributeProposalTemplate("ng-cloak",
                "used to prevent the Angular html template from being briefly displayed by the browser in its raw (uncompiled) form while your application is loading. "
                    + "Use this directive to avoid the undesirable flicker effect caused by the html template display")
                .setAfterAttributeName(""),
            new AngularAttributeProposalTemplate("ng-controller",
                "attaches a controller class to the view. This is a key aspect of how angular supports the principles behind the Model-View-Controller design pattern."),
            new AngularAttributeProposalTemplate("ng-copy", "Specify custom behavior on copy event.")
                .setAllowedTagNames("window", "input", "select", "textarea", "a"),
            new AngularAttributeProposalTemplate("ng-csp", "Enables CSP (Content Security Policy) support.")
                .setAfterAttributeName("")
                .setAllowedTagNames("html"),
            new AngularAttributeProposalTemplate("ng-cut", "Specify custom behavior on cut event.")
                .setAllowedTagNames("window", "input", "select", "textarea", "a"),
            new AngularAttributeProposalTemplate("ng-dblclick", "Specify custom behavior on a dblclick event."),
            new AngularAttributeProposalTemplate("ng-disabled", "Binding expression for disabled attribute")
                .setAllowedTagNames("input"),
            new AngularAttributeProposalTemplate("ng-focus", "Specify custom behavior on focus event.")
                .setAllowedTagNames("window", "input", "select", "textarea", "a"),
            new AngularAttributeProposalTemplate(
                "ng-form",
                "Nestable alias of form directive. HTML does not allow nesting of form elements. It is useful to nest forms, for example if the validity of a sub-group of controls needs to be determined."),
            new AngularAttributeProposalTemplate("ng-hide", "The ngHide directive shows or hides the given HTML element based on the expression provided to the ngHide attribute."),
            new AngularAttributeProposalTemplate("ng-href",
                "Solves the problem of browser making request to url that is not yet resolved by angular and that most likely results in http requests ending up with 404 error.")
                .setAllowedTagNames("a"),
            new AngularAttributeProposalTemplate("ng-if",
                "ngIf differs from ngShow and ngHide in that ngIf completely removes and recreates the element in the DOM rather than changing its visibility via the display css property."),
            new AngularAttributeProposalTemplate("ng-include", "Fetches, compiles and includes an external HTML fragment."),
            new AngularAttributeProposalTemplate(
                "ng-init",
                "The only appropriate use of ngInit is for aliasing special properties of ngRepeat! The ngInit directive allows you to evaluate an expression in the current scope.",
                ngRepeat), // actually ng-init can be used elsewhere as well, but i'm not going to show it as it is discouraged
            new AngularAttributeProposalTemplate("ng-keydown", "Specify custom behavior on keydown event."),
            new AngularAttributeProposalTemplate("ng-keypress", "Specify custom behavior on keypress event."),
            new AngularAttributeProposalTemplate("ng-keyup", "Specify custom behavior on keyup event."),
            new AngularAttributeProposalTemplate("ng-list", "Used to convert and bind text input value to array in model")
                .setAllowedTagNames("input"),
            new AngularAttributeProposalTemplate("ng-model", "Binds html tag to a property on the scope using NgModelController, which is created and exposed by this directive.")
                .setAllowedTagNames("input", "select", "textarea"), // actually in addition tag could be a "custom control" as well (what ever it means, maybe
                                                                    // custom directive)
            new AngularAttributeProposalTemplate("ng-mousedown", "Specify custom behavior on mousedown event."),
            new AngularAttributeProposalTemplate("ng-mouseenter", "Specify custom behavior on mouseenter event."),
            new AngularAttributeProposalTemplate("ng-mouseleave", "Specify custom behavior on mouseleave event."),
            new AngularAttributeProposalTemplate("ng-mousemove", "Specify custom behavior on mousemove event."),
            new AngularAttributeProposalTemplate("ng-mouseover", "Specify custom behavior on mouseover event."),
            new AngularAttributeProposalTemplate("ng-mouseup", "Specify custom behavior on mouseup event."),
            new AngularAttributeProposalTemplate("ng-non-bindable", "The ngNonBindable directive tells Angular not to compile or bind the contents of the current DOM element. "
                + "This is useful if the element contains what appears to be Angular directives and bindings but which should be ignored by Angular. "
                + "This could be the case if you have a site that displays snippets of code, for instance."),
            new AngularAttributeProposalTemplate("ng-open", "Binding expression for details[open]")
                .setAllowedTagNames("details"),
            new AngularAttributeProposalTemplate("ng-paste", "Specify custom behavior on paste event.")
                .setAllowedTagNames("window", "input", "select", "textarea", "a"),
            ngPluralize,
            new AngularAttributeProposalTemplate("ng-readonly", "Binding expression for readonly attribute")
                .setAllowedTagNames("input"),
            ngRepeat,
            new AngularAttributeProposalTemplate("ng-selected", "Binding expression for selected attribute")
                .setAllowedTagNames("option"),
            new AngularAttributeProposalTemplate("ng-show", "show element (<b>using 'ng-hide' CSS class</b>) if given expression evaluates to true"),
            new AngularAttributeProposalTemplate("ng-src",
                "Solves the problem of browser making request to url that is not yet resolved by angular and that most likely results in http requests ending up with 404 error.")
                .setAllowedTagNames("img"),
            new AngularAttributeProposalTemplate(
                "ng-srcset",
                "<b>(NB! srcset is still in draft status in the end of 2013 and is not supported by most browsers)</b> Solves the problem of browser making request to url that is not yet resolved by angular and that most likely results in http requests ending up with 404 error.")
                .setAllowedTagNames("img"),
            new AngularAttributeProposalTemplate("ng-style", "Allows you to set CSS style on an HTML element conditionally."),
            new AngularAttributeProposalTemplate("ng-submit",
                "Enables binding angular expressions to onsubmit events. <b>NB! Make sure that form does not contain an action attribute!</b>")
                .setAllowedTagNames("form"),
            ngSwitch,
            // new AngularAttributeProposalTemplate("on",
            // "Related to " + ngSwitch.getAttributeName() + " - variable to be switched on can be either set by " + ngSwitch.getAttributeName()
            // + " itself or with this attribute (i wouldn't use this attribute).",
            // ngSwitch),
            new AngularAttributeProposalTemplate(
                "ng-transclude",
                "Used in templates of directive that has { restrict: 'E', transclude: true, ...} - in other words: element type directives that can wrap custom content. "
                    + "In directive template you should add 'ng-transclude' attribute to the element that should be replaced by the custom content passed to the directive as directive element body."),

            new AngularAttributeProposalTemplate("ng-value",
                "Binds the given expression to the value of input[select] or input[radio], so that when the element is selected, the ngModel of that element is set to the bound value.")
                .setAllowedTagNames("input")
                .setRequiredAttributesOr("type='select'", "type='radio'"), // TODO make setRequiredAttributesOr work
            new AngularAttributeProposalTemplate("ng-hide", "hide element (using 'ng-hide' CSS class) if given expression evaluates to true")
        );

    Set<AngularAttributeProposalTemplate> attributesForProposing = new TreeSet<AngularAttributeProposalTemplate>();
    addAttributesIfMissing(attributes, attributesForProposing, angularBuiltInDirectiveAttributes);
    if (attributes.getNamedItem("ng-pluralize") != null) {
      addAttributesIfMissing(attributes, attributesForProposing, Arrays.asList(
          // TODO provide better means to set parameters for ngPluralize
          new AngularAttributeProposalTemplate("count", "mandatory attribute for ng-pluralize", ngPluralize),
          new AngularAttributeProposalTemplate("when", "mandatory attribute for ng-pluralize", ngPluralize),
          new AngularAttributeProposalTemplate("offset", "optional attribute for ng-pluralize", ngPluralize)
          ));
    }
    return attributesForProposing;
  }
}