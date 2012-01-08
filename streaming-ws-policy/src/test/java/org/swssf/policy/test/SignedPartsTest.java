/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.swssf.policy.test;

import org.swssf.policy.PolicyEnforcer;
import org.swssf.policy.PolicyViolationException;
import org.swssf.wss.ext.WSSecurityException;
import org.swssf.wss.securityEvent.SecurityEvent;
import org.swssf.wss.securityEvent.SignedPartSecurityEvent;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.xml.namespace.QName;

/**
 * @author $Author: giger $
 * @version $Revision: 1181995 $ $Date: 2011-10-11 20:03:00 +0200 (Tue, 11 Oct 2011) $
 */
public class SignedPartsTest extends AbstractPolicyTestBase {

    @Test
    public void testPolicy() throws Exception {
        String policyString =
                "<sp:SignedParts xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<sp:Body/>\n" +
                        "<sp:Header Name=\"a\" Namespace=\"http://example.org\"/>\n" +
                        "<sp:Attachments>\n" +
                        "<sp3:ContentSignatureTransform/>\n" +
                        "<sp3:AttachmentCompleteSignatureTransform/>\n" +
                        "</sp:Attachments>\n" +
                        "</sp:SignedParts>";
        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);
        SignedPartSecurityEvent signedPartSecurityEvent = new SignedPartSecurityEvent(SecurityEvent.Event.SignedPart, true);
        signedPartSecurityEvent.setElement(new QName("http://schemas.xmlsoap.org/soap/envelope/", "Body"));
        policyEnforcer.registerSecurityEvent(signedPartSecurityEvent);
        signedPartSecurityEvent.setElement(new QName("http://example.org", "a"));
        policyEnforcer.registerSecurityEvent(signedPartSecurityEvent);
        //additional signedParts are also allowed!
        signedPartSecurityEvent.setElement(new QName("http://example.com", "b"));
        policyEnforcer.registerSecurityEvent(signedPartSecurityEvent);
        policyEnforcer.doFinal();
    }

    @Test
    public void testPolicyMultipleAssertionEventsNegative() throws Exception {
        String policyString =
                "<sp:SignedParts xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<sp:Body/>\n" +
                        "<sp:Header Name=\"a\" Namespace=\"http://example.org\"/>\n" +
                        "<sp:Attachments>\n" +
                        "<sp3:ContentSignatureTransform/>\n" +
                        "<sp3:AttachmentCompleteSignatureTransform/>\n" +
                        "</sp:Attachments>\n" +
                        "</sp:SignedParts>";
        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);
        SignedPartSecurityEvent signedPartSecurityEvent = new SignedPartSecurityEvent(SecurityEvent.Event.SignedPart, true);
        signedPartSecurityEvent.setElement(new QName("http://schemas.xmlsoap.org/soap/envelope/", "Body"));
        policyEnforcer.registerSecurityEvent(signedPartSecurityEvent);
        signedPartSecurityEvent = new SignedPartSecurityEvent(SecurityEvent.Event.SignedPart, false);
        signedPartSecurityEvent.setElement(new QName("http://example.org", "a"));
        try {
            policyEnforcer.registerSecurityEvent(signedPartSecurityEvent);
            Assert.fail("Exception expected");
        } catch (WSSecurityException e) {
            Assert.assertTrue(e.getCause() instanceof PolicyViolationException);
        }
    }

    @Test
    public void testPolicyAllHeaders() throws Exception {
        String policyString =
                "<sp:SignedParts xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "</sp:SignedParts>";
        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);
        SignedPartSecurityEvent signedPartSecurityEvent = new SignedPartSecurityEvent(SecurityEvent.Event.SignedPart, true);
        signedPartSecurityEvent.setElement(new QName("http://schemas.xmlsoap.org/soap/envelope/", "Body"));
        policyEnforcer.registerSecurityEvent(signedPartSecurityEvent);
        signedPartSecurityEvent.setElement(new QName("http://example.org", "a"));
        policyEnforcer.registerSecurityEvent(signedPartSecurityEvent);
        signedPartSecurityEvent.setElement(new QName("http://example.com", "b"));
        policyEnforcer.registerSecurityEvent(signedPartSecurityEvent);
        policyEnforcer.doFinal();
    }

    @Test
    public void testPolicyAllHeadersNegative() throws Exception {
        String policyString =
                "<sp:SignedParts xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "</sp:SignedParts>";
        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);
        SignedPartSecurityEvent signedPartSecurityEvent = new SignedPartSecurityEvent(SecurityEvent.Event.SignedPart, false);
        signedPartSecurityEvent.setElement(new QName("http://schemas.xmlsoap.org/soap/envelope/", "Body"));
        try {
            policyEnforcer.registerSecurityEvent(signedPartSecurityEvent);
        } catch (WSSecurityException e) {
            Assert.assertTrue(e.getCause() instanceof PolicyViolationException);
        }
        signedPartSecurityEvent = new SignedPartSecurityEvent(SecurityEvent.Event.SignedPart, false);
        signedPartSecurityEvent.setElement(new QName("http://example.org", "a"));
        try {
            policyEnforcer.registerSecurityEvent(signedPartSecurityEvent);
            Assert.fail("Exception expected");
        } catch (WSSecurityException e) {
            Assert.assertTrue(e.getCause() instanceof PolicyViolationException);
        }
    }

    @Test
    public void testPolicyWildcardHeader() throws Exception {
        String policyString =
                "<sp:SignedParts xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<sp:Body/>\n" +
                        "<sp:Header Namespace=\"http://example.org\"/>\n" +
                        "<sp:Attachments>\n" +
                        "<sp3:ContentSignatureTransform/>\n" +
                        "<sp3:AttachmentCompleteSignatureTransform/>\n" +
                        "</sp:Attachments>\n" +
                        "</sp:SignedParts>";
        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);
        SignedPartSecurityEvent signedPartSecurityEvent = new SignedPartSecurityEvent(SecurityEvent.Event.SignedPart, true);
        signedPartSecurityEvent.setElement(new QName("http://schemas.xmlsoap.org/soap/envelope/", "Body"));
        policyEnforcer.registerSecurityEvent(signedPartSecurityEvent);
        signedPartSecurityEvent.setElement(new QName("http://example.org", "a"));
        policyEnforcer.registerSecurityEvent(signedPartSecurityEvent);
        //additional signedParts are also allowed!
        signedPartSecurityEvent.setElement(new QName("http://example.com", "b"));
        policyEnforcer.registerSecurityEvent(signedPartSecurityEvent);
        policyEnforcer.doFinal();
    }

    @Test
    public void testPolicyWildcardHeaderNegative() throws Exception {
        String policyString =
                "<sp:SignedParts xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<sp:Body/>\n" +
                        "<sp:Header Namespace=\"http://example.org\"/>\n" +
                        "<sp:Attachments>\n" +
                        "<sp3:ContentSignatureTransform/>\n" +
                        "<sp3:AttachmentCompleteSignatureTransform/>\n" +
                        "</sp:Attachments>\n" +
                        "</sp:SignedParts>";
        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);
        SignedPartSecurityEvent signedPartSecurityEvent = new SignedPartSecurityEvent(SecurityEvent.Event.SignedPart, true);
        signedPartSecurityEvent.setElement(new QName("http://schemas.xmlsoap.org/soap/envelope/", "Body"));
        policyEnforcer.registerSecurityEvent(signedPartSecurityEvent);
        signedPartSecurityEvent = new SignedPartSecurityEvent(SecurityEvent.Event.SignedPart, false);
        signedPartSecurityEvent.setElement(new QName("http://example.org", "a"));
        try {
            policyEnforcer.registerSecurityEvent(signedPartSecurityEvent);
            Assert.fail("Exception expected");
        } catch (WSSecurityException e) {
            Assert.assertTrue(e.getCause() instanceof PolicyViolationException);
        }
    }
}