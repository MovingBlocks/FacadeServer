/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.web.authentication;

import org.junit.Test;
import org.terasology.identity.CertificateGenerator;
import org.terasology.identity.CertificatePair;
import org.terasology.identity.IdentityConstants;
import org.terasology.identity.PrivateIdentityCertificate;
import org.terasology.identity.PublicIdentityCertificate;

import java.math.BigInteger;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

public class AuthenticationHandshakeHandlerTest {

    private BigInteger randomBigInteger(Random random) {
        byte[] randomBytes = new byte[64];
        random.nextBytes(randomBytes);
        return new BigInteger(randomBytes);
    }

    private PublicIdentityCertificate randomPublicCert(int seed) {
        Random r = new Random(seed);
        byte[] uuidBytes = new byte[16];
        r.nextBytes(uuidBytes);
        String id = UUID.nameUUIDFromBytes(uuidBytes).toString();
        return new PublicIdentityCertificate(id, randomBigInteger(r), randomBigInteger(r), randomBigInteger(r));
    }

    private PrivateIdentityCertificate randomPrivateCert(int seed) {
        Random r = new Random(seed);
        return new PrivateIdentityCertificate(randomBigInteger(r), randomBigInteger(r));
    }

    @Test(expected = AuthenticationFailedException.class) //authentication attempt that must be rejected
    public void testInvalidCertificate() throws AuthenticationFailedException {
        CertificateGenerator gen = new CertificateGenerator();
        CertificatePair server = gen.generateSelfSigned();

        AuthenticationHandshakeHandler handshake = new AuthenticationHandshakeHandlerImpl(server);
        handshake.initServerHello();
        HandshakeHello clientHello = new HandshakeHello(new byte[4], randomPublicCert(1), 0);
        handshake.authenticate(new ClientAuthenticationMessage(clientHello, null));
    }

    @Test(expected = AuthenticationFailedException.class) //authentication attempt that must be rejected
    public void testBadSignature() throws AuthenticationFailedException {
        CertificateGenerator gen = new CertificateGenerator();
        CertificatePair server = gen.generateSelfSigned();
        CertificatePair client = gen.generate(server.getPrivateCert());

        AuthenticationHandshakeHandler handshake = new AuthenticationHandshakeHandlerImpl(server);
        HandshakeHello serverHello = handshake.initServerHello();
        assertTrue(serverHello.getCertificate().verifySelfSigned());
        byte[] clientRandom = new byte[IdentityConstants.SERVER_CLIENT_RANDOM_LENGTH];
        //correct public certificate...
        HandshakeHello clientHello = new HandshakeHello(clientRandom, client.getPublicCert(), System.currentTimeMillis());
        byte[] dataToSign = HandshakeHello.concat(serverHello, clientHello);
        //...but wrong private certificate
        byte[] signature = randomPrivateCert(2).sign(dataToSign);
        handshake.authenticate(new ClientAuthenticationMessage(clientHello, signature));
    }

    @Test //legitimate authentication attempt that must be accepted
    public void testOk() throws AuthenticationFailedException {
        CertificateGenerator gen = new CertificateGenerator();
        CertificatePair server = gen.generateSelfSigned();
        CertificatePair client = gen.generate(server.getPrivateCert()); //a valid certificate pair signed by the server

        AuthenticationHandshakeHandler handshake = new AuthenticationHandshakeHandlerImpl(server);
        HandshakeHello serverHello = handshake.initServerHello();
        assertTrue(serverHello.getCertificate().verifySelfSigned());
        byte[] clientRandom = new byte[IdentityConstants.SERVER_CLIENT_RANDOM_LENGTH];
        HandshakeHello clientHello = new HandshakeHello(clientRandom, client.getPublicCert(), System.currentTimeMillis());
        byte[] dataToSign = HandshakeHello.concat(serverHello, clientHello);
        byte[] signature = client.getPrivateCert().sign(dataToSign);
        byte[] serverVerification = handshake.authenticate(new ClientAuthenticationMessage(clientHello, signature));
        assertTrue(server.getPublicCert().verify(dataToSign, serverVerification));
    }
}
