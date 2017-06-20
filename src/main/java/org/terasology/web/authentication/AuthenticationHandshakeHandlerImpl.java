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

import org.terasology.config.SecurityConfig;
import org.terasology.identity.CertificatePair;
import org.terasology.identity.IdentityConstants;
import org.terasology.identity.PublicIdentityCertificate;

import java.security.SecureRandom;

/**
 * Represents the server part of an authentication handshake;
 * similar to {@link org.terasology.network.internal.ServerHandshakeHandler}, but more generic (not tied to netty).
 */
public final class AuthenticationHandshakeHandlerImpl implements AuthenticationHandshakeHandler {

    private final CertificatePair serverCertificate;
    private byte[] serverRandom = new byte[IdentityConstants.SERVER_CLIENT_RANDOM_LENGTH];
    private HandshakeHello serverHello;

    public AuthenticationHandshakeHandlerImpl(CertificatePair serverCertificate) {
        this.serverCertificate = serverCertificate;
    }

    public AuthenticationHandshakeHandlerImpl(SecurityConfig securityConfig) {
        this(new CertificatePair(securityConfig.getServerPublicCertificate(), securityConfig.getServerPrivateCertificate()));
    }

    @Override
    public HandshakeHello initServerHello() {
        new SecureRandom().nextBytes(serverRandom);
        serverHello = new HandshakeHello(serverRandom, serverCertificate.getPublicCert(), System.currentTimeMillis());
        return serverHello;
    }

    @Override
    public byte[] authenticate(ClientAuthenticationMessage authenticationMessage) throws AuthenticationFailedException {
        HandshakeHello clientHello = authenticationMessage.getClientHello();
        PublicIdentityCertificate clientCert = clientHello.getCertificate();
        if (!clientCert.verifySignedBy(serverCertificate.getPublicCert())) {
            throw new AuthenticationFailedException(AuthenticationFailedException.INVALID_CLIENT_CERT);
        }
        byte[] signatureData = HandshakeHello.concat(serverHello, clientHello);
        if (!clientCert.verify(signatureData, authenticationMessage.getSignature())) {
            throw new AuthenticationFailedException(AuthenticationFailedException.INVALID_VERIFICATION_SIGNATURE);
        }
        return serverCertificate.getPrivateCert().sign(signatureData);
    }
}
