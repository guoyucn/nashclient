
package org.tempuri;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.tempuri package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _SolveValue_QNAME = new QName("http://tempuri.org/", "value");
    private final static QName _SolveResponseSolveResult_QNAME = new QName("http://tempuri.org/", "solveResult");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.tempuri
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Solve }
     * 
     */
    public Solve createSolve() {
        return new Solve();
    }

    /**
     * Create an instance of {@link SolveResponse }
     * 
     */
    public SolveResponse createSolveResponse() {
        return new SolveResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "value", scope = Solve.class)
    public JAXBElement<String> createSolveValue(String value) {
        return new JAXBElement<String>(_SolveValue_QNAME, String.class, Solve.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tempuri.org/", name = "solveResult", scope = SolveResponse.class)
    public JAXBElement<String> createSolveResponseSolveResult(String value) {
        return new JAXBElement<String>(_SolveResponseSolveResult_QNAME, String.class, SolveResponse.class, value);
    }

}
