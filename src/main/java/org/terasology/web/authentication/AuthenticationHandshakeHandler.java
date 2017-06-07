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

import org.terasology.identity.IdentityConstants;
import org.terasology.identity.PublicIdentityCertificate;

import java.security.SecureRandom;

/**
 * Represents the server part of an authentication handshake;
 * similar to {@link org.terasology.network.internal.ServerHandshakeHandler}, but more generic (not tied to netty).
 */
public final class AuthenticationHandshakeHandler {

    private final PublicIdentityCertificate serverPublicCert;
    private byte[] serverRandom = new byte[IdentityConstants.SERVER_CLIENT_RANDOM_LENGTH];
    private HandshakeHello serverHello;

    public AuthenticationHandshakeHandler(PublicIdentityCertificate serverPublicCert) {
        this.serverPublicCert = serverPublicCert;
    }

    /**
     * @return the initial handshake that must be sent from the server to the client.
     */
    public HandshakeHello initServerHello() {
        new SecureRandom().nextBytes(serverRandom);
        serverHello = new HandshakeHello(serverRandom, serverPublicCert, System.currentTimeMillis());
        return serverHello;
    }

    /**
     * Tries to finalize the authentication process.
     * If no exception is thrown, the authentication has been successfully completed.
     * @param clientHello the hello message sent by the client
     * @param handshakeVerificationSignature the verification data sent by the client. On the client sie it must be built
     * by concatenating the received server hello with the built client hello (see {@link HandshakeHello#concat(HandshakeHello, HandshakeHello)})
     * and signing the result with the private client identity certificate (see {@link org.terasology.identity.PrivateIdentityCertificate#sign(byte[])}).
     * @throws AuthenticationFailedException if the authentication fails
     */
    public void authenticate(HandshakeHello clientHello, byte[] handshakeVerificationSignature) throws AuthenticationFailedException {
        PublicIdentityCertificate clientCert = clientHello.getCertificate();
        if (!clientCert.verifySignedBy(serverPublicCert)) {
            throw new AuthenticationFailedException(true);
        }
        byte[] signatureData = HandshakeHello.concat(serverHello, clientHello);
        if (!clientCert.verify(signatureData, handshakeVerificationSignature)) {
            throw new AuthenticationFailedException(false);
        }
    }
}
