package org.tempuri;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.Action;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * This class was generated by Apache CXF 3.1.4
 * 2016-11-30T14:22:21.783+08:00
 * Generated source version: 3.1.4
 * 
 */
@WebService(targetNamespace = "http://tempuri.org/", name = "IPokerService")
@XmlSeeAlso({com.microsoft.schemas._2003._10.serialization.ObjectFactory.class, ObjectFactory.class})
public interface IPokerService {

    @WebMethod(action = "http://tempuri.org/IPokerService/solve")
    @Action(input = "http://tempuri.org/IPokerService/solve", output = "http://tempuri.org/IPokerService/solveResponse")
    @RequestWrapper(localName = "solve", targetNamespace = "http://tempuri.org/", className = "org.tempuri.Solve")
    @ResponseWrapper(localName = "solveResponse", targetNamespace = "http://tempuri.org/", className = "org.tempuri.SolveResponse")
    @WebResult(name = "solveResult", targetNamespace = "http://tempuri.org/")
    public java.lang.String solve(
        @WebParam(name = "value", targetNamespace = "http://tempuri.org/")
        java.lang.String value
    );
}
